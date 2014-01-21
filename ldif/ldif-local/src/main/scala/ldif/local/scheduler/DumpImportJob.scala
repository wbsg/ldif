/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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
import java.io.{OutputStream, InputStream}
import org.slf4j.LoggerFactory
import java.util.Properties
import ldif.util._
import ldif.datasources.dump.QuadParser
import java.nio.charset.MalformedInputException
import ldif.datasources.dump.parser.ParseException
import scala.Some
import ldif.runtime.Quad

/**
 * Abstract class for dump based import jobs
 * @param dumpLocation dump location, either a local path or a URL
 */
abstract class DumpImportJob(dumpLocation : String) extends ImportJob {

  val graph = Consts.DEFAULT_IMPORTED_GRAPH_PREFIX+id

  private val log = LoggerFactory.getLogger(getClass.getName)

  protected def getInputStream (parameters : Properties = new Properties) : Option[InputStream] = {
      try {
        Some(DumpLoader.getStream(dumpLocation, parameters))
      } catch {
        case e: Exception => {
          val msg = e.getMessage
          log.warn(msg)
          reporter.setStatusMsg(msg)
          None
        }
      }
  }

  /**
   * Perform dump load
   * @param out OutputStream
   * @param estimatedNumberOfQuads Option[Double]
   * @param parameters parameters that will be passed to the dumpLoader
   * @param processQuad function that will be applied to each valid quad found
   * @return Boolean true when the dump is loaded correctly, false otherwise
   */
  def loadDump(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None, parameters : Properties = new Properties, processQuad : (Quad => Quad) = null) : Boolean = {

    JobMonitor.addPublisher(getReporter)
    getReporter.setStartTime()

    getReporter.estimatedQuads = estimatedNumberOfQuads

    val writer = new ReportingOutputStreamWriter(out, getReporter)

    // Get an InputStream from given dump location
    val inputStream = getInputStream(parameters).getOrElse(return false)

    val parser = new QuadParser(graph)
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
          // Skip invalid quads
          invalidQuads  += 1
          getReporter.invalidQuads.incrementAndGet()
          log.debug("Invalid quad found: "+line)
        }
      }
      if (quad != null) {
        if(processQuad != null) {
          quad = processQuad(quad)
        }
        importedQuadsNumber += 1
        writer.write(quad)
      }
    }

    if (invalidQuads>0) {
      log.warn("Invalid quads ("+invalidQuads+") found and skipped in "+ dumpLocation)
    }

    log.debug(importedQuadsNumber + " valid quads loaded from "+id+" ("+dumpLocation+")" )

    writer.flush()
    writer.close()
    reporter.setFinishTime()
    true
  }

  def getReporter : DumpImportJobPublisher

  override def getOriginalLocation = dumpLocation
}

abstract class DumpImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {}

