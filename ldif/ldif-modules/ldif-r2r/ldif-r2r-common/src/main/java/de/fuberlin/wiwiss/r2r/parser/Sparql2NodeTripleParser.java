/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package de.fuberlin.wiwiss.r2r.parser;

import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.RecognitionException;

import de.fuberlin.wiwiss.r2r.*;

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 19:05
 * To change this template use File | Settings | File Templates.
 */

/**
 * Parses a subset of SPARQL to NodeTriple (triples) objects.
 */
public class Sparql2NodeTripleParser {
	public static List<NodeTriple> parse(String sparqlPattern, PrefixMapper prefixMapper) throws RecognitionException {
		CharStream stream =	new ANTLRStringStream(sparqlPattern);
		SPARQL2LDIFLexer lexer = new SPARQL2LDIFLexer(stream);
		TokenStream tokenStream = new CommonTokenStream(lexer);
		SPARQL2LDIFParser parser = new SPARQL2LDIFParser(tokenStream);
		parser.setPrefixMapper(prefixMapper);

		return parser.sourcePattern();
	}

    public static List<NodeTriple> parse(String sparqlPattern) throws RecognitionException {
		return parse(sparqlPattern, new PrefixMapper());
	}
}
