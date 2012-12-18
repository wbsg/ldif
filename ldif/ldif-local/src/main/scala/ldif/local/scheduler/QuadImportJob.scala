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

import ldif.local.datasources.dump.DumpLoader
import xml.Node
import ldif.datasources.dump.QuadParser
import java.io.OutputStream
import org.slf4j.LoggerFactory
import ldif.runtime.Quad
import ldif.datasources.dump.parser.ParseException
import ldif.util._
import java.nio.charset.MalformedInputException

case class QuadImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String, renameGraphs : String = "") extends ImportJob {

  private val log = LoggerFactory.getLogger(getClass.getName)
  val reporter = new QuadImportJobPublisher(id)

  def isRenameGraphEnabled = renameGraphs != ""

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    // ImportJobMonitor.value = reporter
    val writer = new ReportingOutputStreamWriter(out, reporter)

    // get bufferReader from Url
    val inputStream = DumpLoader.getStream(dumpLocation)
		val parser = new QuadParser
		val lines = scala.io.Source.fromInputStream(inputStream)("UTF-8").getLines()
		var invalidQuads = 0

		// Catch dump encoding issues (only UTF-8 is supported)
		val traversableLines =
			try {
				lines.toTraversable
			} catch {
				case e : MalformedInputException => {
					log.warn("An invalid character encoding has been detected for the dump "+dumpLocation+". Please use UTF-8.")
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
          // skip invalid quads
          invalidQuads  += 1
          reporter.invalidQuads.incrementAndGet()
          log.debug("Invalid quad found: "+line)
        }
      }
      if (quad != null) {
        importedQuadsNumber += 1
        if (isRenameGraphEnabled)
          quad = quad.copy(graph = renameGraph(quad.graph))
        importedGraphs += quad.graph
        writer.write(quad.toNQuadFormat+" . \n")
        if (importedGraphs.size >= Consts.MAX_NUM_GRAPHS_IN_MEMORY)
          writeImportedGraphsToFile
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

  /* Rename graph according to a given regex */
  protected def renameGraph(from : String, to : String = "") : String = {
    //    from match {
    //      case re(g, sub) => g
    //      case _ => from
    //    }
    from.replaceAll(renameGraphs, to)
  }

  override def getType = "quad"
  override def getOriginalLocation = dumpLocation

  def toXML = {
    val xml = {
      <quadImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
        {if(isRenameGraphEnabled) <renameGraphs>{renameGraphs}</renameGraphs>}
      </quadImportJob>
    }
    toXML(xml)
  }
}

object QuadImportJob{

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    val renameGraphs = (node \ "renameGraphs" text)
    val job = new QuadImportJob(dumpLocation.trim, id, refreshSchedule, dataSource, renameGraphs)
    job
  }
}

class QuadImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {
  override def getPublisherName = super.getPublisherName + " (quad)"
}