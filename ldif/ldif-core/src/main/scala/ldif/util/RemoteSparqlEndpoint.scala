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

package ldif.util

import io.Source
import java.net._
import org.slf4j.LoggerFactory
import java.io.OutputStreamWriter

/**
 * Executes queries on a remote SPARQL endpoint.
 * Based on the class by the same name from silk-core
 *
 * @param uri The URI of the endpoint
 * @param login The login required by the endpoint for authentication
 * @param pauseTime The minimum number of milliseconds between two queries
 * @param retryCount The number of retries if a query fails
 * @param initialRetryPause The pause in milliseconds before a query is retried. For each subsequent retry the pause is doubled.
 */
class RemoteSparqlEndpoint(val uri: URI,
                           login: Option[(String, String)] = None,
                           val pauseTime: Int = 0,
                           val retryCount: Int = 3, val initialRetryPause: Int = 1000) {


  private val log = LoggerFactory.getLogger(getClass.getName)

  private val url = new URL(uri.toString)

  private var lastQueryTime = 0L

  override def toString = "SparqlEndpoint(" + uri + ")"

  /**
   * Executes a SPARQL SELECT query.
   *
   * @param query The SPARQL query to be executed
   * @return Query result in SPARQL Query Results XML Format
   */
  def executeQuery(query: String) {


    //log.debug("Executing query on " + uri + "\n" + query)

    val connection = openConnection

    val writer = new OutputStreamWriter(connection.getOutputStream, "UTF-8")
    writer.write(query)

    //Close connection
    writer.close()

    //Check if the HTTP response code is in the range 2xx
    if (connection.getResponseCode / 100 == 2) {
      //      log.info("Statements written to Store.")
    }
    else {
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

//    httpConnection = null
//    writer = null

    //Wait until pause time is elapsed since last query
//    synchronized {
//      while (System.currentTimeMillis < lastQueryTime + pauseTime) Thread.sleep(pauseTime / 10)
//      lastQueryTime = System.currentTimeMillis
//    }
//
//    //Execute query
//    log.info("Executing query on " + uri + "\n" + query)
//
//    val url = new URL(uri + "?query=" + URLEncoder.encode(query, "UTF-8"))
//
//    var result: Elem = null
//    var retries = 0
//    var retryPause = initialRetryPause
//    while (result == null) {
//      val httpConnection = RemoteSparqlEndpoint.openConnection(url, login)
//
//      try {
//        result = XML.load(httpConnection.getInputStream)
//      }
//      catch {
//        case ex: IOException => {
//          retries += 1
//          if (retries > retryCount) {
//            throw ex
//          }
//
//          val errorStream = httpConnection.getErrorStream
//          if (errorStream != null) {
//            val errorMessage = Source.fromInputStream(errorStream).getLines.mkString("\n")
//            log.warn("Query on " + uri + " failed:\n" + query + "\nError Message: '" + errorMessage + "'.\nRetrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")")
//          }
//          else {
//            log.warn("Query on " + uri + " failed:\n" + query + "\nRetrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")")
//          }
//
//          Thread.sleep(retryPause)
//          //Double the retry pause up to a maximum of 1 hour
//          //retryPause = math.min(retryPause * 2, 60 * 60 * 1000)
//        }
//        case ex: Exception => {
//          log.error("Could not execute query on " + uri + ":\n" + query, ex)
//          throw ex
//        }
//      }
//    }

    //Return result
 //   log.debug("Query Result\n" + result)
  //  result
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

    //Open connection
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
//    httpConnection.setRequestProperty("ACCEPT", "application/sparql-results+xml")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

    connection
  }
}
