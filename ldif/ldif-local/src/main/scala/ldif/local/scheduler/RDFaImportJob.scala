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

import xml.Node
import org.slf4j.LoggerFactory
import java.io.OutputStream
import ldif.util._
import ldif.local.datasources.dump.DumpLoader
import ldif.datasources.dump.QuadParser
import java.nio.charset.MalformedInputException
import ldif.runtime.Quad
import ldif.datasources.dump.parser.ParseException
import io.Codec._
import java.util.Properties

case class RDFaImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  private val log = LoggerFactory.getLogger(getClass.getName)
  val reporter = new TripleImportJobPublisher(id)

  val graph = Consts.DEFAULT_IMPORTED_GRAPH_PREFIX+id

  override def getType = "html-rdfa"
  override def getOriginalLocation = dumpLocation

  /**
   * @param out OutputStream
   * @param estimatedNumberOfQuads Option[Double]
   * @return Boolean true when the dump is loaded correctly, false otherwise
   */
  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    importedQuadsNumber = 0

    reporter.estimatedQuads = estimatedNumberOfQuads

    val writer = new ReportingOutputStreamWriter(out,reporter)

    // Get bufferReader from Url
    val inputStream =  {
      try {
        // The job id is used to assign a unique URI to the imported resources
        // This is the only difference with the TripleImportJob.load
        val parameters = new Properties()
        parameters.setProperty("jobId", id)
        DumpLoader.getStream(dumpLocation, parameters)
      } catch {
        case e: java.net.ConnectException => return false
      }
    }

    importedGraphs += graph

    val parser = new QuadParser(graph)
    val lines = scala.io.Source.fromInputStream(inputStream)("UTF-8").getLines()
    var invalidQuads = 0

    // Catch dump encoding issues (only UTF-8 is supported)
    val traversableLines =
      try {
        lines.toTraversable
      } catch {
        case e : MalformedInputException => {
          log.warn("An invalid character encoding has been detected for the dump "+dumpLocation+". Please use UTF-8 encoding.")
          Traversable.empty[String]
        }
      }

    for (line <- traversableLines){
      var quad : Quad = null
      try {
        quad = parser.parseLine(line)
      }
      catch {
        case e:ParseException => {
          // Skip invalid quads
          invalidQuads  += 1
          reporter.invalidQuads.incrementAndGet()
          log.debug("Invalid quad found: "+line)
        }
      }
      if(quad != null) {
        importedQuadsNumber += 1
        writer.write(quad)
      }
    }

    if (invalidQuads>0)
      log.warn("Invalid quads ("+invalidQuads+") found and skipped in "+ dumpLocation)

    log.debug(importedQuadsNumber + " valid quads loaded from "+id+" ("+dumpLocation+")" )

    writer.flush
    writer.close
    reporter.setFinishTime()
    true
  }

  override def toXML = {
    val xml = {
      <rdfaImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
      </rdfaImportJob>
    }
    toXML(xml)
  }
}

object RDFaImportJob {

  /**
   * Creates a RDFaImportJob from an XML definition
   * @param node Node
   * @param id Identifier
   * @param refreshSchedule String
   * @param dataSource String
   * @return RDFaImportJob
   */
  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation") text
    val job = new RDFaImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}

class RDFaImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {
  override def getPublisherName = super.getPublisherName + " (html-rdfa)"
}