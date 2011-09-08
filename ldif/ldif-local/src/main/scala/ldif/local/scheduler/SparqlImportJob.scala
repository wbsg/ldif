package ldif.local.scheduler

import ldif.util.Identifier
import java.net.URI
import java.util.logging.Logger
import java.io.{OutputStreamWriter, OutputStream}
import com.hp.hpl.jena.query.QueryExecutionFactory
import ldif.local.runtime.LocalNode
import com.hp.hpl.jena.rdf.model.{Model, RDFNode}

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

    var graph = id
    importedGraphs += graph

    val results = execQuery(query)

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
    writer.close

  }

  override def getType = "sparql"
  override def getOriginalLocation = conf.endpointLocation.toString

  /* Execute a SPARQL query, applying LIMIT and OFFSET if the server limits the result set size  */
  private def execQuery(baseQuery : String) : Model = {
     val endpointUrl = conf.endpointLocation.toString
     var query = baseQuery + " LIMIT " + math.min(conf.pageSize, conf.limit)
     val results = QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct

     var loop = (results.size == conf.pageSize) // && (results.size != conf.limit)
     while (loop) {
       query = baseQuery + " OFFSET " + results.size + " LIMIT " + math.min(conf.pageSize, conf.limit - results.size)
       val res = QueryExecutionFactory.sparqlService(endpointUrl, query).execConstruct
       results.add(res)
       loop = (res.size == conf.pageSize)
     }

    results
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