package ldif.local.scheduler

import ldif.util.Identifier
import java.net.URI
import java.util.logging.Logger
import ldif.local.runtime.LocalNode
import com.hp.hpl.jena.rdf.model.{Model, RDFNode}
import java.io.{Writer, OutputStreamWriter, OutputStream}
import javax.xml.ws.http.HTTPException
import org.apache.http.HttpException
import com.hp.hpl.jena.query.{QueryException, QueryExecutionFactory}

case class SparqlImportJob(conf : SparqlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{
  private val log = Logger.getLogger(getClass.getName)

  override def load(out : OutputStream) {
    val writer = new OutputStreamWriter(out)

    if (conf.endpointLocation == null) {
      log.warning("! Import failed for job "+ id +" - SPARQL endpoint location not defined")
    }

    val query = conf.buildQuery
    log.info("Loading from " + conf.endpointLocation + ", graph: " + conf.graphName)
    log.info("Query: " + query)

    importedGraphs += id

    execQuery(query, writer)

    writer.close
  }

  override def getType = "sparql"
  override def getOriginalLocation = conf.endpointLocation.toString

  /* Execute a SPARQL query, applying LIMIT and OFFSET if the endpoint limits the result set size  */
  private def execQuery(baseQuery : String, writer : Writer)  {
    val endpointUrl = conf.endpointLocation.toString
    var loop = true
    var offset : Long = 0

    while (loop) {
      val query = baseQuery + " OFFSET " + offset + " LIMIT " + math.min(conf.pageSize, conf.limit - offset)

      try {
        val results = QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct
        loop = (results.size == conf.pageSize)
        offset += results.size

        write(results, writer)
      }
      catch {
        case e:Exception => {
          loop = false
          log.warning("Error executing query \n"+query+" \non "+endpointUrl+" \n"+e.getMessage)
        }
      }
      log.info(id +" - loaded "+offset+" quads")

    }
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

case class SparqlConfig(endpointLocation : URI,  graphName : URI, sparqlPatterns : Traversable[String], limit : Int = Integer.MAX_VALUE, pageSize : Int = 1000) {

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