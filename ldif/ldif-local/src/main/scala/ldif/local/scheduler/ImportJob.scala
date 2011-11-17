/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

    writer.write(Quad(jobBlankNode, Consts.rdfTypeProp, Node.createUriNode(Consts.importJobClass), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.importIdProp, Node.createLiteral(id), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.lastUpdateProp, Node.createTypedLiteral(updateTime.toString,"http://www.w3.org/2001/XMLSchema#dateTime"), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasDatasourceProp, Node.createLiteral(dataSource), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasImportTypeProp, Node.createLiteral(getType), provenanceGraph).toLine)
    writer.write(Quad(jobBlankNode, Consts.hasOriginalLocationProp, Node.createLiteral(getOriginalLocation), provenanceGraph).toLine)

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