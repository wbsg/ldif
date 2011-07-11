package ldif.local

import datasources.dump.QuadParser
import runtime._
import java.io.File
import collection.mutable.{HashMap, MultiMap, Set}
import java.util.logging.Logger

/*
* Compare LDIF output with a given LDImpoter output (which has a target vocab and minting)
*/

object OutputValidator {

  private val log = Logger.getLogger(getClass.getName)

  def compare(ldifOutputFile : File, otherOutputFile : File) : Int =  {
    log.info("Output validation started")

    val parser = new QuadParser
    val ldifOutputQuads = scala.io.Source.fromFile(ldifOutputFile).getLines.toTraversable.map(parser.parseLine(_))
    val otherOutputQuads = scala.io.Source.fromFile(otherOutputFile).getLines.toTraversable.map(parser.parseLine(_))

    val otherHT:MultiMap[String, Pair[String,String]] = new HashMap[String, Set[Pair[String,String]]] with MultiMap[String, Pair[String,String]]
    val sameAsMap = new HashMap[String,String]
    for (quad <- otherOutputQuads)
      if (quad!=null) {
        if (quad.predicate.equals("http://www.w3.org/2002/07/owl#sameAs"))
          sameAsMap.put(quad.value.toString,quad.subject.toString)
        else
          otherHT.addBinding(quad.predicate, Pair(quad.subject.toString,quad.value.toString))
      }

    val ldifHT:MultiMap[String, Pair[String,String]] = new HashMap[String, Set[Pair[String,String]]] with MultiMap[String, Pair[String,String]]
    for (quad <- ldifOutputQuads)
      if (quad!=null)
        // filter out provenance triples
        //TODO should be configurable - see #37
        if (!quad.graph.equals("http://www4.wiwiss.fu-berlin.de/ldif/provenance"))
        {
          var sub = quad.subject.toString
          if (sameAsMap.contains(sub))
            sub = sameAsMap.get(sub).get
          var obj = quad.value.toString
          if (sameAsMap.contains(obj))
            obj = sameAsMap.get(obj).get
          ldifHT.addBinding(quad.predicate, Pair(sub,obj))
        }

    var err = 0

    // check #1: ldif-output => ldimporter-output
    for (p <- ldifHT.keys){
      if (otherHT.contains(p)){
        val elems = otherHT.get(p).get
        for (elem <- ldifHT.get(p).get.toSeq)
          if(!elems.contains(elem)) {
            err += 1
            log.warning("Quad not found: "+elem._1+" <"+p+"> "+elem._2)
          }
      }
      else {
        err += ldifHT.get(p).get.size
        log.warning("Property not found: "+p)
      }
    }

    // check #2: ldimporter-output => ldif-output
    for (p <- otherHT.keys){
      if (ldifHT.contains(p)){
        val elems = ldifHT.get(p).get
        for (elem <- otherHT.get(p).get.toSeq)
          if(!elems.contains(elem)) {
            err += 1
            log.warning("Quad not found: "+elem._1+" <"+p+"> "+elem._2)
          }
      }
      else {
        err += otherHT.get(p).get.size
        log.warning("Property not found: "+p)
      }
    }

    if (err == 0)
      log.info("Output is correct")
    else log.warning("Output is NOT correct. "+ err +" error(s) found.")

    err
  }


  def contains(ldifOutputFile:File, quads : Traversable[Quad]) = {
    val parser = new QuadParser

    val isContained = new Array[Boolean](quads.size)

    val lines = scala.io.Source.fromFile(ldifOutputFile).getLines

    for (oq <- lines.toTraversable.map(parser.parseLine(_))){
      for ((q,i) <- quads.toSeq.zipWithIndex)
        if (oq.equals(q))
          isContained(i) = true
    }

    //log.warning("Quads missing: "+isContained.count(false))
    isContained.filter(x => !x).isEmpty
  }

}
