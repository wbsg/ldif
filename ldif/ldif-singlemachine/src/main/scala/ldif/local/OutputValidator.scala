package ldif.local

import runtime._
import java.io.File
import collection.mutable.{ListBuffer, ArrayBuffer}

/*
 * Compare LDIF output with a given LDImpoter output (which has a target vocab and minting) 
 */

object OutputValidator {

  def compare (reader:QuadReader, cOutputFile : File) {
    // read the correct output - from file
    val lines = scala.io.Source.fromFile(cOutputFile).getLines
    val cOutput = new ListBuffer[String]
    val cSameAsOutput = new ListBuffer[String]
    for (l <- lines.toSeq){
      if ((l.split(" "))(1).equals("<http://www.w3.org/2002/07/owl#sameAs>"))
        cSameAsOutput += l
      else cOutput += l
    }

    // read LDIF output - from QuadReader
    val ldifOutput = new ArrayBuffer[String](reader.size)
    while(!reader.isEmpty)       {
      // workaround - nxparser seems not to parse escapes properly - see #18
      var triple = reader.read.toNQuadFormat.replace("\\\"","\"")
      // nquad > ntriples
      triple = triple.substring(0,triple.lastIndexOf("<")) + "."
      ldifOutput += triple
    }

    // init vars
    val outSize = ldifOutput.size+cOutput.size
    var eq = false
    var errCount = 0
    var count = 0

    // check #1: ldif-output => ldimporter-output
    for (line <- ldifOutput){

      eq = lineCompare(line,cOutput,cSameAsOutput,true)

      if (!eq) {
        errCount = errCount +1
        println(line)
      }
      count = count +1
      if(count%200==0)
        {println(count*100/(outSize)+"%")
          if (errCount>0) printf("("+errCount+" errors found)")
        }
    }

    // check #2: ldimporter-output => ldif-output
    for (line <- cOutput){

      eq = lineCompare(line,ldifOutput,cSameAsOutput,false)

      if (!eq) {
        println(line)
        errCount= errCount+1
      }
      count = count +1
      if(count%200==0)
        {println(count*100/(ldifOutput.size+cOutput.size)+"%")
          if (errCount>0) printf("("+errCount+" errors found)")
        }
    }

    if (errCount == 0 )
      println("\nOutput is correct")
    else println("\nOutput is NOT correct. "+ errCount +" errors found.")
  }


  private def getSameAs(node : String, sameAsList : Seq[String], direction :Boolean ) = {
    val same = new ListBuffer[String]
    for (l <- sameAsList){
      val split = l.split(" ",3)
      if (direction) {
        if (split(2).equals(node))
          same +=  split(0)       }
      else
      if (split(0).equals(node))
        same +=  split(2).substring(0,split(2).size-2)
    }
    same
  }

  private def lineCompare(line : String, output : Seq[String], cSameAsOutput : Seq[String], direction : Boolean) ={
    val triple = line.split(" ",3)

    var subjSame : ListBuffer[String] = null
    var objSame :  ListBuffer[String] = null
    if (direction){
      subjSame = getSameAs(triple(0)+" .",cSameAsOutput,direction)
      objSame = getSameAs(triple(2),cSameAsOutput,direction)

    }
    else {
      subjSame = getSameAs(triple(0),cSameAsOutput,direction)
      objSame = getSameAs(triple(2).substring(0,triple(2).size-2),cSameAsOutput,direction)
    }

    var eq = false
    for (l1 <- output){
      if(!eq){
        val split1 = l1.split(" ",3)
        if (triple(1).equals(split1(1))){
          if (l1.equals(line))  {eq = true}
          else {
            for (l3 <- subjSame){
              if(!eq)
                if (l1.equals(l3 +" "+triple(1)+" "+triple(2))) { eq = true }
              for (l4 <- objSame){
                if(!eq)
                  if (l1.equals(l3 +" "+triple(1)+" "+l4+" ."))  { eq = true }
              }
            }
            for (l4 <- objSame){
              if(!eq){
                if (l1.equals(triple(0)+" "+triple(1)+" "+l4+" ."))  { eq = true }
              }
            }
          }
        }

      }
    }
    eq
  }
}
