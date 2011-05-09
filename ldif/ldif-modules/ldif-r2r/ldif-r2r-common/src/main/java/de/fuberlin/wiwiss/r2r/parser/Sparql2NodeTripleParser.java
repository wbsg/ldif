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
