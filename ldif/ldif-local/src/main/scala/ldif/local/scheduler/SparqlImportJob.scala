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

package ldif.local.scheduler

import java.net.URI
import org.slf4j.LoggerFactory
import ldif.local.runtime.LocalNode
import com.hp.hpl.jena.rdf.model.{Model, RDFNode}
import java.io.{Writer, OutputStreamWriter, OutputStream}
import com.hp.hpl.jena.query.QueryExecutionFactory
import ldif.util.{Consts, Identifier}
import javax.xml.ws.http.HTTPException
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP

case class SparqlImportJob(conf : SparqlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{
  private val log = LoggerFactory.getLogger(getClass.getName)
  private val graph = Consts.DEFAULT_IMPORTED_GRAPH_PREFIX+id

  override def load(out : OutputStream) : Boolean = {
    val writer = new OutputStreamWriter(out)

    if (conf.endpointLocation == null) {
      log.warn("Import failed for job "+ id +" - SPARQL endpoint location not defined")
    }

    val query = conf.buildQuery
    log.info("Loading from " + conf.endpointLocation)
    log.debug("Query: " + query)

    importedGraphs += graph

    val success = execQuery(query, writer)
    writer.close
    success
  }

  override def getType = "sparql"
  override def getOriginalLocation = conf.endpointLocation.toString

  /* Execute a SPARQL query, applying LIMIT and OFFSET if the endpoint limits the result set size  */
  private def execQuery(baseQuery : String, writer : Writer) : Boolean = {
    val endpointUrl = conf.endpointLocation.toString
    var loop = true
    var offset : Long = 0
    var limit : Long = Consts.maxPageSize

    while (loop) {
      var query = baseQuery
      if (offset > 0)
        query += " OFFSET " + offset + " LIMIT " + math.min(limit, conf.tripleLimit - offset)
      else query +=  " LIMIT " + math.min(limit, conf.tripleLimit)

      var retries = 1
      var retryPause = Consts.retryPause
      val retryCount = Consts.retryCount
      var results : Model = null
      while (results == null) {
        try {
          results = QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct
        }
        catch {
          case e: HTTPException => {
            // stop on client side errors
            if(e.getStatusCode < 500 && e.getStatusCode >= 400) {
              log.warn("Error executing query: " + query + ". Error Code: " + e.getStatusCode + " (" + e.getMessage + ")")
              return false
            }

            log.warn("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+" \n"+e.getCause)
            retries += 1
            if (retries > retryCount) {
              return false
            }
            Thread.sleep(retryPause)
            retryPause *= 2
          }
          case e: QueryExceptionHTTP => {
            // stop on client side errors
            if(e.getResponseCode < 500 && e.getResponseCode >= 400) {
              log.warn("Error executing query: " + query + ". Error Code: " + e.getResponseCode + "(" + e.getResponseMessage + ")")
              return false
            }
            log.warn("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+" \n"+e.getResponseMessage)
            retries += 1
            if (retries > retryCount) {
              return false
            }
            Thread.sleep(retryPause)
            retryPause *= 2
          }
        }
      }
      if (offset == 0) {
         // set page size for the sparql endpoint
         limit = results.size
      }
      offset += results.size
      loop = (results.size == limit) && !(offset == conf.tripleLimit)
      write(results, writer)
      if (loop)
        log.info(id +" - loaded "+offset+" quads")
    }
    true
  }

  /* Write SPARQL results */
  private def write(results : Model, writer : Writer)  {
    //results.write(out, "N-QUAD", graph)
    val stmtIterator = results.listStatements
    while (stmtIterator.hasNext){
      val stmt = stmtIterator.next
      val s = LocalNode.fromRDFNode(stmt.getSubject.asInstanceOf[RDFNode])
      val p = stmt.getPredicate.getURI
      val o = LocalNode.fromRDFNode(stmt.getObject)
      writer.write(s.toNQuadsFormat +" <"+ p +"> "+ o.toNQuadsFormat +" <"+ graph +"> . \n")
    }
    writer.flush
  }
}

object SparqlImportJob {

  def fromXML(node : xml.Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val endpointLocation = new URI((node \ "endpointLocation") text)
    val graphName = new URI((node \ "graphName") text)

    val tripleLimitString =  ((node \ "tripleLimit") text)
    var tripleLimit = Long.MaxValue
    if(tripleLimitString.length > 0)
      tripleLimit = tripleLimitString.toLong

    val sparqlPatterns = (node \ "sparqlPatterns" \ "pattern").map(x => x text).toTraversable

    val sparqlConfig = SparqlConfig(endpointLocation, graphName, sparqlPatterns, tripleLimit)
    val job = new SparqlImportJob(sparqlConfig, id, refreshSchedule, dataSource)
    job
  }

}

case class SparqlConfig(endpointLocation : URI,  graphName : URI, sparqlPatterns : Traversable[String], tripleLimit : Long = Long.MaxValue) {

  def buildQuery : String = {
    val isGraphDefined = graphName.toString.trim != ""

    var query = "CONSTRUCT { ?s ?p ?o }\n" + "WHERE {\n"

    if (isGraphDefined) {
      query += " GRAPH <" + graphName + "> {\n"
    }

    query += " ?s ?p ?o .\n"

    for (pattern <- sparqlPatterns.filterNot(_.trim.equals(""))) {
        query += " " + pattern + " .\n"
    }

    if (isGraphDefined) {
      query += " }\n"
    }
    query += "}\n"

    query
  }

}