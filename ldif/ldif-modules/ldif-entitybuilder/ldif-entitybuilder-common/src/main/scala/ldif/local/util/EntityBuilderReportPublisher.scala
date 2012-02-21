package ldif.local.util

import collection.mutable.ArrayBuffer
import ldif.util.{Report, ReportItem, ReportPublisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

object EntityBuilderReportPublisher extends ReportPublisher {
  private var quadsReadCounter = 0
  private var finishedReading = false
  private var sameAsQuadCounter = 0
  private var entityDescriptionsProcessed = 0

  def getPublisherName = "Entity Builder"

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    if(finishedReading)
      reportItems.append(ReportItem("Loading quads", "Done", quadsReadCounter + " quads loaded"))
    else {
      if(quadsReadCounter==0)
        reportItems.append(ReportItem("Loading quads", "Not started yet", "-"))
      else
        reportItems.append(ReportItem("Loading quads", "Running...", quadsReadCounter + " quads loaded"))
    }

    return Report(reportItems)
  }

  def initialize = {}
}