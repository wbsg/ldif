/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.output

import org.slf4j.LoggerFactory
import ldif.util.Consts
import java.io.OutputStreamWriter
import io.Source
import java.net._
import ldif.runtime.{QuadWriter, Quad}

/**
 * Output writer which writes to a SPARQL/Update endpoint
 */

case class SparqlWriter(uri: String,
                        login: Option[(String, String)] = None,
                        sparqlVersion : String = Consts.SparqlUpdateVersionDefault,
                        useDirectPost : Boolean = Consts.SparqlUseDirectPostDefault.toBoolean,
                        queryParameter: String = Consts.SparqlQueryParameterDefault) extends QuadWriter {

  private val log = LoggerFactory.getLogger(getClass.getName)

  // init
  var statements = 0
  var queryContent= new StringBuilder
  var queryGraph : String = null
  var quadGraph : String = null

  override def write(quad : Quad) {

    quadGraph = quad.graph.toString

    // execute query
    if (statements == Consts.SparqlMaxStatmentsPerRequest || (quadGraph != queryGraph && statements > 0))
    {
      // TODO optimization: collect quads from same graph
      buildAndExecuteQuery(queryContent.toString(), queryGraph)
      // reset query parameters
      statements = 0
      queryContent = new StringBuilder()
    }

    queryContent.append(quad.toNTripleFormat +" . ")
    statements += 1
    queryGraph = quadGraph
  }

  override def finish() {
    if (statements > 0)
      buildAndExecuteQuery(queryContent.toString(), queryGraph)
  }

  /**
   * Builds and executes SPARQL/Update queries.
   * Creates a new named graph if required.
   *
   * @param content The statements to be inserted
   * @param graph The named graph
   */

  private def buildAndExecuteQuery(content: String, graph: String = null) {
     if (graph != null) {
      if (sparqlVersion == "1.0") {
        executeQuery("CREATE SILENT GRAPH <" + graph + ">")
        executeQuery("INSERT DATA INTO <" + graph + "> { " + content + " }")
      }
      else {
        executeQuery("INSERT DATA { GRAPH <" + graph + "> { " + content + " } }")
      }
    }
    else
      executeQuery("INSERT DATA { " + content + " }")
  }

  /**
   * Executes a single SPARQL/Update query
   *
   * @param query The SPARQL query to be executed
   */
  private def executeQuery(query: String) {

    log.debug("Executing query on " + uri + "\n" + query)

    val encodedQuery = encodeQuery(query)

    val connection = openConnection
    val writer = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
    writer.write(encodedQuery)
    writer.close()

    //Check if the HTTP response code is in the range 2xx
    if (connection.getResponseCode / 100 != 2) {
      val errorStream = connection.getErrorStream
      log.error("Error exectuing query: "+query)
      if (errorStream != null) {
        val errorMessage = Source.fromInputStream(errorStream).getLines.mkString("\n")
        log.warn("SPARQL/Update query on " + uri + " failed. Error Message: '" + errorMessage + "'.")
      }
      else {
        log.warn("SPARQL/Update query on " + uri + " failed. Server response: " + connection.getResponseCode + " " + connection.getResponseMessage + ".")
      }
    }
  }

  private def encodeQuery(content : String) : String = {
    if (useDirectPost)
      content
    else
      queryParameter+"="+URLEncoder.encode(content, "UTF-8")
  }

  /**
   * Opens a new HTTP connection to the endpoint.
   * This method is synchronized to avoid race conditions as the Authentication is set globally in Java.
   */
  private def openConnection() : HttpURLConnection = synchronized {
    //Set authentication
    for ((user, password) <- login) {
      Authenticator.setDefault(new Authenticator() {
        override def getPasswordAuthentication = new PasswordAuthentication(user, password.toCharArray)
      })
    }

    //Open a new HTTP connection
    val url = new URL(uri)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    if (useDirectPost)
      connection.setRequestProperty("Content-Type", "application/sparql-update")
    else
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    connection.setRequestProperty("Accept", "application/rdf+xml")

    connection
  }

}