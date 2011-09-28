package ldif.local.scheduler

import ldif.runtime.Quad
import ldif.entity.Node
import collection.mutable.{HashSet, ListBuffer, Set}
import java.util.Date
import xml.XML
import ldif.util.{ValidatingXMLReader, Consts, Identifier}
import java.io._
import java.util.logging.Logger

trait ImportJob {
  private val log = Logger.getLogger(getClass.getName)
  val id : Identifier
  val refreshSchedule : String
  val dataSource : String
  // Contains the list of the imported graphs
  var importedGraphs : Set[String] = new HashSet[String]
  // Used to tmp store the list of the imported graphs, if there are too many graphs
  var importedGraphsFile : File = null

  def load(out : OutputStream) : Boolean

  def getType : String
  def getOriginalLocation : String

  /* Build provenance quads for the import job */
  def generateProvenanceInfo(writer : Writer, provenanceGraph : String) =  {

    //log.info("Building provenance metadata for "+id)

    // build xsd datetime
    val now = new Date
    val updateTime = new StringBuffer(Consts.xsdDateTimeFormat.format(now))
    updateTime.insert(22, ':')

    //TODO create an unique blank node
    val jobBlankNode = Node.createBlankNode(id, provenanceGraph)

    val quads = new ListBuffer[Quad]
    quads.append(Quad(jobBlankNode, Consts.rdfTypeProp, Node.createUriNode(Consts.importJobClass), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.importIdProp, Node.createLiteral(id), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.lastUpdateProp, Node.createTypedLiteral(updateTime.toString,"http://www.w3.org/2001/XMLSchema#dateTime"), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasDatasourceProp, Node.createLiteral(dataSource), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasImportTypeProp, Node.createLiteral(getType), provenanceGraph))
    quads.append(Quad(jobBlankNode, Consts.hasOriginalLocationProp, Node.createLiteral(getOriginalLocation), provenanceGraph))

    // add graphs
    val importedGraph = Node.createUriNode(Consts.importedGraphClass)
    for (g <- importedGraphs.map(Node.createUriNode(_))) {
      quads.append(Quad(g, Consts.hasImportJobProp, jobBlankNode, provenanceGraph))
      quads.append(Quad(g, Consts.rdfTypeProp, importedGraph, provenanceGraph))
    }

    for (quad <- quads)
      writer.write(quad.toNQuadFormat+" . \n")

    // add graphs from file (in case)
    if (importedGraphsFile != null && importedGraphsFile.exists) {
      val lines = scala.io.Source.fromFile(importedGraphsFile).getLines
      for (g <- lines.map(Node.createUriNode(_))) {
        writer.write(Quad(g, Consts.hasImportJobProp, jobBlankNode, provenanceGraph).toNQuadFormat+" . \n")
        writer.write(Quad(g, Consts.rdfTypeProp, importedGraph, provenanceGraph).toNQuadFormat+" . \n")
      }
      importedGraphsFile.delete
    }

    writer.flush
    writer.close
    now
  }

  /* Write importedGraphs to a temporary file and empty importedGraphs */
  protected def writeImportedGraphsToFile {
    if (importedGraphsFile == null) {
      log.warning("Imported dump for "+id+" contains more than "+ Consts.MAX_NUM_GRAPHS_IN_MEMORY +" different graphs. Provenance metadata could contain duplicates.")
      importedGraphsFile= File.createTempFile(id+"_importedGraph_"+Consts.simpleDateFormat.format(new Date()),"")
    }
    // append graph names to tmp file
    val writer = new FileWriter(importedGraphsFile, true)
    for (g <- importedGraphs)
      writer.write(g+"\n")
    writer.close
    importedGraphs = HashSet.empty[String]
  }
}

object ImportJob {

  private val schemaLocation = "xsd/ImportJob.xsd"

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(file : File) = {
    fromXML(XML.loadFile(file))
  }

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