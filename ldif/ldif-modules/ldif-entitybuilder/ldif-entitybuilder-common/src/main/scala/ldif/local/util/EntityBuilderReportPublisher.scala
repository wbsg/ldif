package ldif.local.util

import collection.mutable.ArrayBuffer
import ldif.util.{Report, ReportItem, ReportPublisher}
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityBuilderReportPublisher(var name: String) extends ReportPublisher {
  var quadsReadCounter = new AtomicInteger(0)
  var finishedReading = false
  var entityQueuesFilled = new AtomicInteger(0)
  var finishedBuilding = false
  var entitiesBuilt = new AtomicInteger(0)

  def getPublisherName = name

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    if(finishedReading) {
      reportItems.append(ReportItem("Loading quads", "Done", quadsReadCounter + " quads loaded"))
      val buildStatus = if(finishedBuilding) "Done" else "-"
      reportItems.append(ReportItem("Building entities", buildStatus, entityQueuesFilled.get() + " entity queues finished"))
    }
    else {
      if(quadsReadCounter==0)
        reportItems.append(ReportItem("Loading quads", "Not started yet", "-"))
      else
        reportItems.append(ReportItem("Loading quads", "Running...", quadsReadCounter + " quads loaded"))
    }
    if(entitiesBuilt.get > 0 || finishedBuilding)
      reportItems.append(ReportItem("Entities built", "-", entitiesBuilt.toString))
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }

    return Report(reportItems)
  }
}