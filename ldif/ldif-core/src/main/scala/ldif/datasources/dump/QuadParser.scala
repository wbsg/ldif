package ldif.datasources.dump

import java.util.logging.Logger
import ldif.local.runtime.Quad
import org.antlr.runtime.{CommonTokenStream, ANTLRStringStream}
import parser.{NQuadParser, NQuadLexer}

/**
 * Created by IntelliJ IDEA.
 * User: andrea
 * Date: 8/4/11
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadParser(graphURI: String) {
  def this() {
    this("default")
  }

  /**
   * returns a Quad object if string can be parsed as Quad or null for comment line
   * @throws: ParseException
   */
  def parseLine(line: String): Quad = {
    val stream = new ANTLRStringStream(line)
		val lexer = new NQuadLexer(stream)
		val tokenStream = new CommonTokenStream(lexer)
		val parser = new NQuadParser(tokenStream)
    parser.setGraph(graphURI)
    parser.line()
  }
}