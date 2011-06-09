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
  val quadParser = new QuadFileParser(graphURI)

  def this() {
    this("default")
  }

  def parseNTLine(line: String): Quad = {
    quadParser.parseAll(quadParser.line, line) match {
      case quadParser.Success(quad, _) => quad
      case _ => null
    }
  }

  def parseQuadLine(line: String): Quad = {
    quadParser.parseAll(quadParser.line, line) match {
      case quadParser.Success(quad, _) => quad
      case _ => null
    }
  }

  def readQuads(input: BufferedReader, quadQueue: QuadWriter) {
    var line: String = null

    var loop = true

    while(loop) {
      val line = input.readLine()

      if(line==null)
        loop = false
      else {
        val quad = parseNTLine(line)
        if(quad!=null) {
          quadQueue.write(quad) }
      }
    }
  }
}