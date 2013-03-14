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

package ldif.local.scheduler

import java.net.URI
import org.slf4j.LoggerFactory
import ldif.local.runtime.LocalNode
import java.io.{Writer, OutputStream}
import com.hp.hpl.jena.query.{ResultSet, QueryExecutionFactory}
import javax.xml.ws.http.HTTPException
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP
import ldif.util._
import ldif.runtime.Quad
import com.hp.hpl.jena.shared.JenaException
import org.xml.sax.SAXParseException

case class SparqlImportJob(conf : SparqlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{
  private val log = LoggerFactory.getLogger(getClass.getName)
  val reporter = new SparqlImportJobPublisher(id)

  def isTripleLimitEnabled = conf.tripleLimit != Long.MaxValue

  var statusMsg = ""

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    reporter.estimatedQuads = estimatedNumberOfQuads

    if (isTripleLimitEnabled) {
      reporter.queryLimit = conf.tripleLimit.toInt
    }

    val writer = new ReportingOutputStreamWriter(out, reporter)

    var success = false

    if (conf.endpointLocation == null) {
      statusMsg = "Import failed for job "+ id +" - SPARQL endpoint location not defined"
    } else {
      val query = conf.buildQuery
      log.info("Loading from " + conf.endpointLocation)
      log.debug("Query: " + query)
      success = execQuery(query, writer)
    }

    writer.close()

    if (!success) {
      if (!statusMsg.isEmpty){
        log.warn(statusMsg)
        reporter.setStatusMsg(statusMsg)
      }
    } else {
      reporter.setFinishTime()
    }

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
      if (offset > 0) {
        query += " OFFSET " + offset + " LIMIT " + math.min(limit, conf.tripleLimit - offset)
      } else {
        query +=  " LIMIT " + math.min(limit, conf.tripleLimit)
      }

      var retries = 1
      var retryPause = Consts.retryPause
      val retryCount = Consts.retryCount
      var results : ResultSet = null
      try{
        while (results == null) {
          try {
            results = QueryExecutionFactory.sparqlService(endpointUrl, query).execSelect()
          }
          catch {
            case e: HTTPException => {
              // stop on client side errors
              if(e.getStatusCode < 500 && e.getStatusCode >= 400) {
                statusMsg = "Error executing query: " + query + ". Error Code: " + e.getStatusCode + " (" + e.getMessage + ")"
                return false
              }

              log.warn("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+(if(e.getCause!=null) " \n"+e.getCause else ""))
              retries += 1
              if (retries > retryCount) {
                statusMsg = e.getMessage
                return false
              }
              Thread.sleep(retryPause)
              retryPause *= 2
            }
            case e: QueryExceptionHTTP => {
              // stop on client side errors
              if(e.getResponseCode < 500 && e.getResponseCode >= 400) {
                statusMsg = "Error executing query: " + query + ". Error Code: " + e.getResponseCode + "(" + e.getResponseMessage + ")"
                return false
              }
              log.warn("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+" \n"+e.getMessage)
              retries += 1
              if (retries > retryCount) {
                statusMsg = e.getMessage
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
        val count = write(results, writer)
        if (offset == 0) {
           // set page size for the sparql endpoint
           limit = count
        }
        offset += count
        loop = (count == limit) && (limit != 0) && !(offset == conf.tripleLimit)

        if (count!=0 || limit == 0) {
          log.info(id +" - loaded "+offset+" quads")
        }
      }
    }

    importedQuadsNumber = offset
    true
  }

  /* Write SPARQL results */
  private def write(results : ResultSet, writer : Writer): Int =  {
    //results.write(out, "N-QUAD", graph)
    var counter = 0
    while(results.hasNext) {
      counter += 1
      val solution = results.next()
      val s = LocalNode.fromRDFNode(solution.get("s"))
      val p = LocalNode.fromRDFNode(solution.get("p")).value
      val o = LocalNode.fromRDFNode(solution.get("o"))
      val g = if (conf.isGraphDefined) conf.graphName.toString else LocalNode.fromRDFNode(solution.get("g")).value
      importedGraphs += g
      val quad = Quad(s,p,o,g)
      writer.write(quad.toLine)
    }
    writer.flush()
    counter
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
    if(tripleLimitString.length > 0) {
      tripleLimit = tripleLimitString.toLong
    }

    val sparqlPatterns = (node \ "sparqlPatterns" \ "pattern").map(x => x.text).toTraversable

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
    var query = "SELECT ?s ?p ?o "
    if(!isGraphDefined) {
      query += "?g "
    }

    query += "\n" + "WHERE {\n"

    if (isGraphDefined) {
      query += " GRAPH <" + graphName + "> {\n"
    } else {
      query += " GRAPH ?g {\n"
    }

    for (pattern <- sparqlPatterns.filterNot(_.trim.equals(""))) {
      query += " " + pattern + " .\n"
    }

    query += " ?s ?p ?o \n}}\n"
    query
  }

}

class SparqlImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {

  var queryLimit : Int = 0

  override def getPublisherName = super.getPublisherName + " (sparql)"

  override def getReport : Report = {
    var customReportItems = Seq.empty[ReportItem]
    if(queryLimit > 0) {
      customReportItems = customReportItems :+ ReportItem.get("Query limit", queryLimit)
    }
    super.getReport(customReportItems)
  }

}