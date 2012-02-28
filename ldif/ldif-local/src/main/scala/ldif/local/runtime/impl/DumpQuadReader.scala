package ldif.local.runtime.impl

import java.io.File
import ldif.runtime.Quad
import ldif.local.runtime.{ConfigParameters, QuadReader}
import ldif.util.{ReportPublisher, Consts}

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
  private val provenanceGraph = config.configProperties.getProperty("provenanceGraph", Consts.DEFAULT_PROVENANCE_GRAPH)
  private val outputFormat = config.configProperties.getProperty("outputFormat", "nq").toLowerCase
  private val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
  private val ignoreProvenance = !(outputFormat=="nq" || outputFormat=="sparql")

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
      if(!filterQuad(quad)) {
        bufferedQuad = quad
        return true
      }
    }
    return false
  }

  /**
   * Filter sameAs and provenance quads from the input AND output
   * quads as defined in the config.
   */
  private def filterQuad(quad: Quad): Boolean = {
    if(isProvenanceQuad(quad)) {
      if(!ignoreProvenance)
        config.provenanceQuadsWriter.write(quad)
      return true
    } else if(quad.predicate=="http://www.w3.org/2002/07/owl#sameAs"){
      if(useExternalSameAsLinks)
        config.sameAsWriter.write(quad)
      return true
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

class DumpLoadReportPublisher extends ReportPublisher { //TODO
  def getPublisherName = "Dump Loader"

  def getReport = null
}