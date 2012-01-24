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

import ldif.util.RemoteSparqlEndpoint
import org.slf4j.LoggerFactory
import ldif.runtime.Quad
import java.net.{URLEncoder, URI}


case class SparqlWriter(uri: String, login: Option[(String, String)] = None, parameter: String = "query") extends OutputWriter {

  private val log = LoggerFactory.getLogger(getClass.getName)

  private val endpoint = new RemoteSparqlEndpoint(new URI(uri), login)

  // maximum number of statements per request
  private val statementsPerRequest = 500

  // init
  var statements = 0
  var queryContent= new StringBuilder
  var queryGraph : String = null
  var quadGraph : String = null

  override def write(quad : Quad) {

    quadGraph = quad.graph.toString

    // execute query
    if (statements == statementsPerRequest || (quadGraph != queryGraph && statements > 0))
    {
      query(queryContent.toString(), queryGraph)
      // reset query parameters
      statements = 0
      queryContent = new StringBuilder()
    }

    queryContent.append(quad.toNTripleFormat +" . ")
    statements += 1
    queryGraph = quadGraph
  }

  override def close() {
    if (statements > 0)
      query(queryContent.toString(), queryGraph)
  }

  private def query(content : String, graph : String = null) {

    if (graph != null) {
      //endpoint.executeQuery("CREATE SILENT GRAPH <" + graph + ">")
      val q = parameter+"="+URLEncoder.encode("INSERT DATA { GRAPH <" + graph + "> { " + content + " } }", "UTF-8")
      //log.info(q)
      endpoint.executeQuery(q)

    }
    else
      endpoint.executeQuery(parameter+"="+URLEncoder.encode("INSERT DATA { " + content + " }", "UTF-8"))

  }
}