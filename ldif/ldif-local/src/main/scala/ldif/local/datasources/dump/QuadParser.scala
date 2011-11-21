/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local.datasources.dump

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import ldif.datasources.dump.parser.{NQuadParser, NQuadLexer}
import ldif.runtime.Quad
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