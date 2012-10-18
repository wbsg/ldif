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

import ldif.runtime.Quad
import ldif.entity.Node
import collection.mutable.{HashSet, Set}
import java.util.Date
import xml.XML
import java.io._
import org.slf4j.LoggerFactory
import ldif.util._
import scala.Some
import ldif.runtime.Quad

trait ImportJob {
  private val log = LoggerFactory.getLogger(getClass.getName)
  val reporter : Publisher
  val id : Identifier
  val refreshSchedule : String
  val dataSource : String
  // Contains the list of the imported graphs
  var importedGraphs : Set[String] = new HashSet[String]
  // Used to tmp store the list of the imported graphs, if there are too many graphs
  var importedGraphsFile : File = null
  // The number of imported quads
  var importedQuadsNumber : Double = 0

  /**
   * Start import and write results to output stream. Return true on success and true and fail.
   */
  def load(out : OutputStream, estimatedNumberOfQuads : Option[Double]) : Boolean

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

    writer.write(Quad(jobBlankNode, Consts.rdfTypeProp, Node.createUriNode(Consts.importJobClass), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.importIdProp, Node.createLiteral(id), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.lastUpdateProp, Node.createTypedLiteral(updateTime.toString,"http://www.w3.org/2001/XMLSchema#dateTime"), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasDatasourceProp, Node.createLiteral(dataSource), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasImportTypeProp, Node.createLiteral(getType), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasOriginalLocationProp, Node.createLiteral(getOriginalLocation), provenanceGraph).toLine)
    var importedGraphsNumber : Int = importedGraphs.size
    if(importedGraphsFile != null && importedGraphsFile.exists)
      importedGraphsNumber += scala.io.Source.fromFile(importedGraphsFile).getLines().size
    importedQuadsNumber += importedGraphsNumber*2 + 7 // add number of provenance quads
    writer.write(Quad(jobBlankNode, Consts.numberOfQuadsProp, Node.createTypedLiteral(importedQuadsNumber.intValue().toString,Consts.xsdNonNegativeInteger),provenanceGraph).toLine)

    // add graphs
    val importedGraph = Node.createUriNode(Consts.importedGraphClass)
    for (g <- importedGraphs.map(Node.createUriNode(_))) {
      writer.write(Quad(g, Consts.hasImportJobProp, jobBlankNode, provenanceGraph).toLine)
      writer.write(Quad(g, Consts.rdfTypeProp, importedGraph, provenanceGraph).toLine)
    }

    // add graphs from file (in case)
    if (importedGraphsFile != null && importedGraphsFile.exists) {
      val lines = scala.io.Source.fromFile(importedGraphsFile).getLines
      for (g <- lines.map(Node.createUriNode(_))) {
        writer.write(Quad(g, Consts.hasImportJobProp, jobBlankNode, provenanceGraph).toLine)
        writer.write(Quad(g, Consts.rdfTypeProp, importedGraph, provenanceGraph).toLine)
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
      log.warn("Imported dump for "+id+" contains more than "+ Consts.MAX_NUM_GRAPHS_IN_MEMORY +" different graphs. Provenance metadata could contain duplicates.")
      importedGraphsFile= TemporaryFileCreator.createTemporaryFile(id+"_importedGraph_"+Consts.simpleDateFormat.format(new Date()),"")
    }
    // append graph names to tmp file
    val writer = new FileWriter(importedGraphsFile, true)
    for (g <- importedGraphs)
      writer.write(g+"\n")
    writer.close
    importedGraphs = HashSet.empty[String]
  }

  def toXML : xml.Node

  def toXML(core : xml.Node): xml.Node = {
    <importJob xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www4.wiwiss.fu-berlin.de/ldif/ ../../xsd/ImportJob.xsd"
            xmlns="http://www4.wiwiss.fu-berlin.de/ldif/">
      <internalId>{id}</internalId>
      <dataSource>{dataSource}</dataSource>
      <refreshSchedule>{refreshSchedule}</refreshSchedule>
      {core}
    </importJob>
  }
}

object ImportJob {

  private val schemaLocation = "xsd/ImportJob.xsd"

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(file : File) = {
    fromXML(XML.loadFile(file))
  }

  def fromString(xmlString : String) = {
    fromXML(XML.loadString(xmlString))
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

    (node \ "wikidataImportJob").headOption match {
      case Some(job) => return WikidataImportJob.fromXML(job, id, refreshSchedule, dataSource)
      case None =>
    }

    null
  }

}