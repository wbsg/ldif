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

package ldif.local.scheduler

import java.net.URI
import org.slf4j.LoggerFactory
import ldif.local.runtime.LocalNode
import com.hp.hpl.jena.rdf.model.{Model, RDFNode}
import java.io.{Writer, OutputStream}
import com.hp.hpl.jena.query.QueryExecutionFactory
import javax.xml.ws.http.HTTPException
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP
import ldif.util._
import ldif.runtime.Quad
import com.hp.hpl.jena.shared.JenaException
import org.xml.sax.SAXParseException

case class SparqlImportJob(conf : SparqlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{
  private val log = LoggerFactory.getLogger(getClass.getName)
  private val graph = Consts.DEFAULT_IMPORTED_GRAPH_PREFIX+id
  val reporter = new SparqlImportJobPublisher(id)
  JobMonitor.addPublisher(reporter)

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    reporter.setStartTime()
    reporter.estimatedQuads = estimatedNumberOfQuads

    val writer = new ReportingOutputStreamWriter(out, reporter)

    if (conf.endpointLocation == null) {
      log.warn("Import failed for job "+ id +" - SPARQL endpoint location not defined")
    }

    val query = conf.buildQuery
    log.info("Loading from " + conf.endpointLocation)
    log.debug("Query: " + query)

    importedGraphs += graph

    val success = execQuery(query, writer)
    writer.close
    reporter.setFinishTime()
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
      try{
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
      } catch {
        case e: JenaException => {
          if(e.getCause.isInstanceOf[SAXParseException]) {
            offset += 100 // skip over bad value
            log.warn("Parse error: Trying to skip over bad results")
          }
        }
      }

      if(results!=null){
        if (offset == 0) {
           // set page size for the sparql endpoint
           limit = results.size
           reporter.actualLimit = limit.toInt
        }
        offset += results.size
        loop = (results.size == limit) && (limit != 0) && !(offset == conf.tripleLimit)
        write(results, writer)
        if (results.size!=0 || limit == 0)
          log.info(id +" - loaded "+offset+" quads")
      }
    }

    importedQuadsNumber = offset.toDouble
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
      val quad = Quad(s,p,o,graph)
      writer.write(quad.toLine)
    }
    writer.flush
  }

  def toXML = {
    val xml = {
      <sparqlImportJob>
        <endpointLocation>{conf.endpointLocation}</endpointLocation>
        {if (conf.isTripleLimitDefined) <tripleLimit>{conf.tripleLimit}</tripleLimit>}
        {if (conf.isGraphDefined) <graphName>{conf.graphName}</graphName>}
        {if (conf.isAnyPatternDefined){
            <sparqlPatterns>
              {for (pattern <- conf.sparqlPatterns) yield { <pattern>{pattern}</pattern> } }
            </sparqlPatterns>}
        }
      </sparqlImportJob>
    }
    toXML(xml)
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

case class SparqlConfig(endpointLocation : URI,  graphName : URI, sparqlPatterns : Traversable[String], tripleLimit : Long = Consts.SparqlTripleLimitDefault) {

  def isTripleLimitDefined = tripleLimit!=Consts.SparqlTripleLimitDefault
  def isGraphDefined = graphName.toString.trim != ""
  def isAnyPatternDefined = sparqlPatterns.size>0

  def buildQuery : String = {
    var query = "CONSTRUCT { ?s ?p ?o }\n" + "WHERE {\n"

    if (isGraphDefined) {
      query += " GRAPH <" + graphName + "> {\n"
    }

    for (pattern <- sparqlPatterns.filterNot(_.trim.equals(""))) {
        query += " " + pattern + " .\n"
    }

    query += " ?s ?p ?o \n"

    if (isGraphDefined) {
      query += " }\n"
    }
    query += "}\n"

    query
  }

}

class SparqlImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {

  //var limit : Int = 0
  var actualLimit : Int = 0

  override def getPublisherName = super.getPublisherName + " (sparql)"

  override def getReport : Report = {
    val customReportItems = Seq(ReportItem.get("Query limit",actualLimit))
    super.getReport(customReportItems)
  }

}