package ldif.local.runtime.impl

import java.io.File
import ldif.runtime.Quad
import ldif.local.runtime.{ConfigParameters, QuadReader}
import java.util.concurrent.atomic.AtomicInteger
import collection.mutable.ArrayBuffer
import ldif.util._
import ldif.local.IntegrationJobStatusMonitor

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
//  private val outputFormat = config.configProperties.getProperty("outputFormat", "nq").toLowerCase
  private val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
//  private val ignoreProvenance = !(outputFormat=="nq" || outputFormat=="sparql")//TODO: implement this feature in output component
  private val reporter = new DumpLoadReportPublisher(useExternalSameAsLinks)
  IntegrationJobStatusMonitor.value.addPublisher(reporter)

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
   * Filter sameAs and provenance quads from the input AND output
   * quads as defined in the config.
   */
  private def filterQuad(quad: Quad): Boolean = {
    if(isProvenanceQuad(quad)) {
      config.provenanceQuadsWriter.write(quad)
      reporter.provenanceQuads.incrementAndGet()
      return true
    } else if(quad.predicate=="http://www.w3.org/2002/07/owl#sameAs"){
      if(useExternalSameAsLinks) {
        config.sameAsWriter.write(quad)
        reporter.externalSameAsQuads.incrementAndGet()
      }
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

class DumpLoadReportPublisher(val useSameAs: Boolean) extends ReportPublisher {
  var loadedQuads = new AtomicInteger(0)
  var externalSameAsQuads = new AtomicInteger(0)
  var provenanceQuads = new AtomicInteger(0)

  def getPublisherName = "Dump Loader"

  private def createSameAsReportItem: ReportItem = {
    if (useSameAs)
      ReportItem("Nr. of sameAs links extracted", "store", externalSameAsQuads + " quads")
    else
      ReportItem("Nr. of sameAs links extracted", "ignore", "-")
  }

  private def createProvenanceReportItem: ReportItem = {
    ReportItem("Nr. of provenance quads extracted", "store", provenanceQuads + " quads")
  }

  private def createLoadedQuadsReportItem: ReportItem = {
    val status = if(finished)
      "Done"
    else
      "Loading..."
    ReportItem("Nr. of quads loaded", status, loadedQuads + " quads")
  }

  def getReport: Report = {
    val reports = new ArrayBuffer[ReportItem]
    reports.append(getStartTimeReportItem)
    reports.append(createLoadedQuadsReportItem)
    reports.append(createSameAsReportItem)
    reports.append(createProvenanceReportItem)
    if(finished) {
      reports.append(getFinishTimeReportItem)
      reports.append(getDurationTimeReportItem)
    }

    return Report(reports)
  }
}