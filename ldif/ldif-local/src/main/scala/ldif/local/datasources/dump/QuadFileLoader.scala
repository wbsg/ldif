package ldif.local.datasources.dump

import ldif.local.runtime.{QuadWriter, Quad}
import java.util.logging.Logger
import scala.collection.mutable.{ArrayBuffer, Map}
import scala.Predef._
import java.io.{File, BufferedReader}
import java.io.FileReader

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 09.06.11
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */

class QuadFileLoader(graphURI: String) {
  private val log = Logger.getLogger(getClass.getName)
  val quadParser = new QuadParser(graphURI)

  def this() {
    this("default")
  }

  def parseQuadLine(line: String): Quad = {
    quadParser.parseLine(line) match {
      case quad: Quad => quad
      case _ => null
    }
  }

  def validateQuads(input: BufferedReader): Seq[Pair[Int, String]] = {
    val errorList = new ArrayBuffer[Pair[Int, String]]
    var lineNr = 1

    var line: String = input.readLine()
    while(line!=null) {
      try {
        parseQuadLine(line)
      } catch {
        case e: RuntimeException => {
          errorList += Pair(lineNr, line)
        }
      }
      line = input.readLine()
      lineNr += 1
    }

    errorList
  }

  def readQuads(input: BufferedReader, quadQueue: QuadWriter) {
    var loop = true
    var foundParseError = false

    var counter = 1;
    var nrOfErrors = 0;

    while(loop) {
      val line = input.readLine()
      var quad: Quad = null

      if(line==null)
        loop = false
      else {
        try {
          quad = parseQuadLine(line)
        } catch {
          case e: RuntimeException => {
            foundParseError=true
            nrOfErrors += 1
            log.warning("Parse error found at line " + counter + ". Input line: " + line)
          }
        }
        if(quad!=null && (!foundParseError)) {
          quadQueue.write(quad) }
      }
      counter += 1
    }
    if(foundParseError)
      throw new RuntimeException("Found errors while parsing NT/NQ file. Found " + nrOfErrors + " parse errors. See log file for more details.")
  }
}
