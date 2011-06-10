package ldif.local.datasources.dump

import java.io.BufferedReader
import ldif.local.runtime.{QuadWriter, Quad}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 09.06.11
 * Time: 17:15
 * To change this template use File | Settings | File Templates.
 */

class QuadFileLoader(graphURI: String) {
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
    var line: String = null

    var loop = true

    var counter = 1;
    while(loop) {
      val line = input.readLine()

      if(line==null)
        loop = false
      else {
        val quad = parseQuadLine(line)
        if(quad!=null) {
          quadQueue.write(quad) }
      }
      counter += 1
      if(counter % 100000 == 0)
        System.err.println(counter)
    }
  }
}