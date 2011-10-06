package ldif.local.scheduler

import java.net.URI
import java.util.logging.Logger
import ldif.local.runtime.LocalNode
import com.hp.hpl.jena.rdf.model.{Model, RDFNode}
import java.io.{Writer, OutputStreamWriter, OutputStream}
import com.hp.hpl.jena.query.QueryExecutionFactory
import ldif.util.{Consts, Identifier}
import javax.xml.ws.http.HTTPException
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP

case class SparqlImportJob(conf : SparqlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{
  private val log = Logger.getLogger(getClass.getName)

  override def load(out : OutputStream) : Boolean = {
    val writer = new OutputStreamWriter(out)

    if (conf.endpointLocation == null) {
      log.warning("! Import failed for job "+ id +" - SPARQL endpoint location not defined")
    }

    val query = conf.buildQuery
    log.info("Loading from " + conf.endpointLocation + ", graph: " + conf.graphName)
    log.info("Query: " + query)

    importedGraphs += id

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

    while (loop) {
      val query = baseQuery + " OFFSET " + offset + " LIMIT " + math.min(conf.pageSize, conf.limit - offset)

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
              log.warning("Error executing query: " + query + ". Error Code: " + e.getStatusCode + " (" + e.getMessage + ")")
              return false
            }

            log.warning("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+" \n"+e.getMessage)
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
              log.warning("Error executing query: " + query + ". Error Code: " + e.getResponseCode + "(" + e.getResponseMessage + ")")
              return false
            }
            log.warning("Error executing query - retrying in " + retryPause + " ms. (" + retries + "/" + retryCount + ")\n"+query+" \non "+endpointUrl+" \n"+e.getResponseMessage)
            retries += 1
            if (retries > retryCount) {
              return false
            }
            Thread.sleep(retryPause)
            retryPause *= 2
          }
        }
      }
      loop = (results.size == conf.pageSize)
      offset += results.size
      write(results, writer)
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
      writer.write(s.toNQuadsFormat +" <"+ p +"> "+ o.toNQuadsFormat +" <"+ id +"> . \n")
    }
    writer.flush
  }
}

object SparqlImportJob {

  def fromXML(node : xml.Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val endpointLocation = new URI((node \ "endpointLocation") text)
    val graphName = new URI((node \ "graphName") text)
    val sparqlPatterns = (node \ "sparqlPatterns" \ "pattern").map(x => x text).toTraversable

    val sparqlConfig = SparqlConfig(endpointLocation, graphName, sparqlPatterns)
    val job = new SparqlImportJob(sparqlConfig, id, refreshSchedule, dataSource)
    job
  }

}

case class SparqlConfig(endpointLocation : URI,  graphName : URI, sparqlPatterns : Traversable[String], limit : Int = Integer.MAX_VALUE, pageSize : Int = Consts.pageSize) {

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