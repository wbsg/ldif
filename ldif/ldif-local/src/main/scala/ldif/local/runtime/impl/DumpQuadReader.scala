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

package ldif.local.runtime.impl

import ldif.runtime.{QuadReader, Quad}
import ldif.local.runtime.ConfigParameters
import ldif.util._
import ldif.local.IntegrationJobMonitor
import ldif.local.report.DumpLoadReportPublisher

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/28/12
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This quad reader filters quads of an encapsulated quad reader according to config parameters.
 * It also writes out certain quads, like sameAs etc.
 */
class DumpQuadReader(inputQuadReader: QuadReader, config: ConfigParameters) extends QuadReader {
  private var bufferedQuad: Quad = null
  private val outputAllQuads = config.configProperties.getProperty("output", "mapped-only").toLowerCase=="all"
  private val provenanceGraph = config.configProperties.getProperty("provenanceGraphURI", Consts.DEFAULT_PROVENANCE_GRAPH)
  private val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
  val reporter = new DumpLoadReportPublisher(useExternalSameAsLinks)
  IntegrationJobMonitor.value.addPublisher(reporter)

  def size: Int = inputQuadReader.size

  def read(): Quad = {
    val returnQuad = bufferedQuad
    bufferedQuad = null
    return returnQuad
  }

  def hasNext: Boolean = {
    if(bufferedQuad!=null)
      return true

    while(inputQuadReader.hasNext) {
      val quad = inputQuadReader.read()
      reporter.loadedQuads.incrementAndGet()
      if(!filterQuad(quad)) {
        bufferedQuad = quad
        return true
      }
    }
    reporter.setFinishTime
    return false
  }

  /**
   * Filter provenance quads from the input AND output
   * quads as defined in the config.
   */
  private def filterQuad(quad: Quad): Boolean = {
    if(isProvenanceQuad(quad)) {
      config.provenanceQuadsWriter.write(quad)
      reporter.provenanceQuads.incrementAndGet()
      return true // Only filter provenance quads from the input
    } else if(quad.predicate==Consts.SAMEAS_URI){
      if(useExternalSameAsLinks) {
        config.sameAsWriter.write(quad)
        reporter.externalSameAsQuads.incrementAndGet()
      }
    } else if(outputAllQuads)
      config.otherQuadsWriter.write(quad)
    return false // Don't filter
  }


  private def isProvenanceQuad(quad: Quad): Boolean = {
    if(quad.graph==provenanceGraph)
      true
    else
      false
  }
}

