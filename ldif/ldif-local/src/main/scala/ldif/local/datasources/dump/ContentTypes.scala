/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import java.util.regex.Pattern

object ContentTypes {

	val CONTENTTYPES_RDFXML = Seq("application/rdf+xml" /* official */, "text/rdf+xml", "text/xml", "application/xml")
	val CONTENTTYPES_RDFN3 = Seq("text/n3" /* proposed */, "application/n3", "text/rdf+n3")
	val CONTENTTYPES_RDFTTL = Seq("text/turtle" /* proposed */, "application/x-turtle" /* pre-registration */, "application/turtle")
	val CONTENTTYPES_RDFNTRIPLE = Seq("text/plain")
	/* Note: This is provided redundantly so that it qualifies as a constant expression */
	val CONTENTTYPES_RDF = "application/rdf+xml,text/rdf+xml,text/xml,application/xml,text/n3,application/n3,text/rdf+n3,text/turtle,application/x-turtle,application/turtle,text/plain"
	val CONTENTTYPES_CSV = Seq("text/csv")
  val CONTENTTYPES_HTML = Seq("text/html")


	// from com.hp.hpl.jena.util.FileUtils
	val langXML = "RDF/XML"
	val langXMLAbbrev = "RDF/XML-ABBREV"
	val langNTriple = "N-TRIPLE"
	val langNQuad = "N-QUAD"
	val langN3 = "N3"
	val langTurtle = "TURTLE"
	// additional formats
	val langCSV = "CSV"
	val langHTML = "HTML"

	val HTTP_ACCEPT_CONTENT_TYPES =  "text/plain;q=1," +  /* N-TRIPLE */
		"text/n3;q=0.9,application/n3;q=0.9,text/rdf+n3;q=0.9,"+  /* N3 */
		"text/turtle;q=0.9,application/x-turtle;q=0.9,application/turtle;q=0.9," +  /* TURTLE */
		"application/rdf+xml;q=0.7,text/rdf+xml;q=0.7,application/xml;q=0.5,text/xml;q=0.5";  /* RDF/XML */

	private def check(needle:String, haystack:Traversable[String]) = {
		var result:Boolean = false
		for (element <- haystack) {
			if (needle.startsWith(element)) {
				result = true
			}
		}
		result
	}

	/**
	 * see http://www.w3.org/TR/REC-rdf-syntax/#section-MIME-Type
	 * @param contentType
	 * @return Boolean true, if the provided content type is indicative of RDF/XML content
	 */
	def isRDFXML(contentType:String) = check(contentType, CONTENTTYPES_RDFXML)

	/**
	 * @see http://www.w3.org/DesignIssues/Notation3.html
	 * @param contentType
	 * @return Boolean true, if the provided content type is indicative of RDF/N3 content
	 */
	def isRDFN3(contentType:String) = check(contentType, CONTENTTYPES_RDFN3)

	/**
	 * @see	http://www.w3.org/TeamSubmission/turtle/
	 * @param contentType
	 * @return Boolean true, if the provided content type is indicative of RDF/TTL content
	 */
	def isRDFTTL(contentType:String) = check(contentType, CONTENTTYPES_RDFTTL)

	/**
	 * @param contentType
	 * @return Boolean true, if the provided content type is indicative of RDF content
	 */
	def isRDFNTriple(contentType:String) = check(contentType, CONTENTTYPES_RDFNTRIPLE)

	/**
	 * @param contentType
	 * @return Boolean true, if the provided content type is indicative of CSV content
	 */
	def isCSV(contentType:String) = check(contentType, CONTENTTYPES_CSV)

	/**
	 * Translates a given content type to lang
	 * @param contentType
	 * @return String
	 */
	def getLangFromContentType(contentType:String):String = {
		var result:String = null
		if (contentType == null || isRDFXML(contentType)) {
			result = langXML
		} else if (isRDFN3(contentType)) {
			result = langN3
		} else if (isRDFNTriple(contentType)) {
			result = langNTriple
		} else if (isRDFTTL(contentType)) {
			result = langTurtle
		} else if (isCSV(contentType)) {
			result = langCSV
		}
		result
	}

	/**
	 * Guesses a content type based on the file extension in an URL or a local path
	 * Supports chained extensions, e.g. somefile.nt.bz2
	 * @param urlOrPath
	 * @return String
	 */
	def getLangFromExtension(urlOrPath:String):String = {
		var result:String = null
		if (Pattern.matches(".*\\.(rdf|rdfxml|xml|owl)(\\..*)?", urlOrPath)) {
			result = langXML
		} else if (Pattern.matches(".*\\.n3(\\..*)?", urlOrPath)) {
			result = langN3
		} else if (Pattern.matches(".*\\.nt(\\..*)?", urlOrPath)) {
			result = langNTriple
		} else if (Pattern.matches(".*\\.nq(\\..*)?", urlOrPath)) {
			result = langNQuad
		} else if (Pattern.matches(".*\\.ttl(\\..*)?", urlOrPath)) {
			result = langTurtle
		} else if (Pattern.matches(".*\\.csv(\\..*)?", urlOrPath)) {
			result = langCSV
		} else if (Pattern.matches(".*\\.html(\\..*)?", urlOrPath)) {
			result = langHTML
		}
		result
	}

	/**
	 * Translates a given lang to content type
	 * @return String
	 */
	def getContentTypeFromLang(lang : String) : String = {
		if (lang == langXML) {
			CONTENTTYPES_RDFXML.head
		} else if (lang == langN3) {
			CONTENTTYPES_RDFN3.head
		} else if (lang == langTurtle) {
			CONTENTTYPES_RDFTTL.head
		} else if (lang == langCSV) {
			CONTENTTYPES_CSV.head
        } else if (lang == langHTML) {
			CONTENTTYPES_HTML.head
		} else "text/plain"
	}

}
