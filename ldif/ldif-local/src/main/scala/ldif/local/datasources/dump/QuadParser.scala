package ldif.local.datasources.dump

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import parser.{NQuadParser, NQuadLexer}
import ldif.local.runtime.Quad
;

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10.06.11
 * Time: 16:31
 * To change this template use File | Settings | File Templates.
 */

class QuadParser(val graphURI: String) {
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