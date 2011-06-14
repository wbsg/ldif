package ldif.local.datasources.dump

import java.io.BufferedReader
import ldif.local.runtime.{QuadWriter, Quad}
import java.util.logging.Logger

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
  //      if(counter % 100000 == 0)
  //        System.err.println(counter)
    }
    if(foundParseError)
      throw new RuntimeException("Found errors while parsing NT/NQ file. Found " + nrOfErrors + " parse errors. See log file for more details.")
  }
}