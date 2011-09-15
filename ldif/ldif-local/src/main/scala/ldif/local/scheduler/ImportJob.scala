package ldif.local.scheduler

import ldif.runtime.Quad
import ldif.entity.Node
import collection.mutable.{HashSet, ListBuffer, Set}
import java.io._
import java.util.Date
import ldif.util.{Consts, Identifier}

trait ImportJob {
  val id : Identifier
  val refreshSchedule : String
  val dataSource : String
  // Contains the list of the imported graphs
  var importedGraphs : Set[String] = new HashSet[String]

  def load(out : OutputStream) : Boolean

  def getType : String
  def getOriginalLocation : String

  /* Build provenance quads for the import job */
  def generateProvenanceInfo(writer : Writer, provenanceGraph : String) =  {

    // build xsd datetime
    val now = new Date
    val updateTime = new StringBuffer(Consts.xsdDateTimeFormat.format(now))
    updateTime.insert(22, ':')

    //TODO create an unique blank node
    val jobBlankNode = Node.createBlankNode(id.replace(".",""), provenanceGraph)

    val quads = new ListBuffer[Quad]

    // add graphs
    val importedGraph = Node.createUriNode(Consts.importedGraphClass)
    for (g <- importedGraphs.map(Node.createUriNode(_))) {
      quads.append(Quad(g, Consts.hasImportJobProp, jobBlankNode, provenanceGraph))
      quads.append(Quad(g, Consts.rdfTypeProp, importedGraph, provenanceGraph))
    }

    quads.append(Quad(jobBlankNode, Consts.rdfTypeProp, Node.createUriNode(Consts.importJobClass), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.importIdProp, Node.createLiteral(id), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.lastUpdateProp, Node.createTypedLiteral(updateTime.toString,"http://www.w3.org/2001/XMLSchema#dateTime"), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasDatasourceProp, Node.createLiteral(dataSource), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasImportTypeProp, Node.createLiteral(getType), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasOriginalLocationProp, Node.createLiteral(getOriginalLocation), provenanceGraph))


    for (quad <- quads)
      writer.write(quad.toNQuadFormat+" . \n")

    writer.flush
    writer.close
    now
  }
}

object ImportJob {

  /* Build an Import Job from XML config */
  // - assume only one import job is defined
  def fromXML(node : xml.Node) : ImportJob = {
    val id = (node \ "internalId" text)
    val dataSource = (node \ "dataSource" text)
    val refreshSchedule = (node \ "refreshSchedule" text)

    (node \ "quadImportJob").headOption match {
      case Some(job) => return QuadImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    (node \ "tripleImportJob").headOption match {
      case Some(job) => return TripleImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    (node \ "crawlImportJob").headOption match {
      case Some(job) => return CrawlImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    (node \ "sparqlImportJob").headOption match {
      case Some(job) => return SparqlImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    null
  }

}