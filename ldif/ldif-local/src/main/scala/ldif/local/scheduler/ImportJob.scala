package ldif.local.scheduler

import ldif.util.Identifier
import ldif.local.runtime.Quad
import ldif.entity.Node
import collection.mutable.{HashSet, ListBuffer, Set}
import java.io._
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import ldif.local.util.Const

trait ImportJob {
  val id : Identifier
  val refreshSchedule : String
  val dataSource : String
  // Contains the list of the imported graphs
  var importedGraphs : Set[String] = new HashSet[String]

  def load(file : Writer)

  def getType : String
  def getOriginalLocation : String

  /* Build provenance quads for the import job */
  def generateProvenanceInfo(writer : Writer, provenanceGraph : String)    {
    val hasImportJobProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob"
    val importIdProp = "http://www4.wiwiss.fu-berlin.de/ldif/importId"
    val lastUpdateProp = "http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"
    val hasDatasourceProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource"
    val hasImportTypeProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportType"
    val hasOriginalLocationProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation"


    // build xsd datetime
    val updateTime = new StringBuffer(Const.xsdDateTimeFormat.format(new Date))
    updateTime.insert(22, ':')

    //TODO create an unique blank node
    val jobBlankNode = Node.createBlankNode(id.replace(".",""), provenanceGraph)

    val quads = new ListBuffer[Quad]

    // add graphs
    for (g <- importedGraphs.map(Node.createUriNode(_))) {
      quads.append(Quad(g, hasImportJobProp, jobBlankNode, provenanceGraph))
    }

    quads.append(Quad(jobBlankNode, importIdProp, Node.createLiteral(id), provenanceGraph))
    quads.append(Quad(jobBlankNode, lastUpdateProp, Node.createTypedLiteral(updateTime.toString,"http://www.w3.org/2001/XMLSchema#dateTime"), provenanceGraph))
    quads.append(Quad(jobBlankNode, hasDatasourceProp, Node.createLiteral(dataSource), provenanceGraph))
    quads.append(Quad(jobBlankNode, hasImportTypeProp, Node.createLiteral(getType), provenanceGraph))
    quads.append(Quad(jobBlankNode, hasOriginalLocationProp, Node.createLiteral(getOriginalLocation), provenanceGraph))


    for (quad <- quads)
      writer.write(quad.toNQuadFormat+" . \n")

    writer.flush
    writer.close
  }
}

object ImportJob {

  /* Build an Import Job from XML config */
  // - assume only one import job is defined
  def fromXML(node : xml.Node) : ImportJob = {
    val id = node \ "internalId" text
    val dataSource = node \ "dataSource" text
    val refreshSchedule = node \ "refreshSchedule" text

    (node \ "quadImportJob").headOption match {
      case Some(job) => return QuadImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    (node \ "tripleImportJob").headOption match {
      case Some(job) => return TripleImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

//    (node \ "crawlImportJob").headOption match {
//      case Some(job) => return CrawlImportJob.fromXML(job, id, refreshSchedule, dataSource)
//      case None =>
//    }

//    (node \ "sparqlImportJob").headOption match {
//      case Some(job) => return SparqlImportJob.fromXML(job, id, refreshSchedule, dataSource)
//      case None =>
//    }

    null
  }

}