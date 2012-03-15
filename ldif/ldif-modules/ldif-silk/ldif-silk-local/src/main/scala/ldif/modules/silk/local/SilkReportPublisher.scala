package ldif.modules.silk.local

import collection.mutable.ArrayBuffer
import ldif.util.{ReportItem, Report, ReportPublisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/8/12
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */

class SilkReportPublisher extends ReportPublisher {
  def getPublisherName = "Silk"

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }

    return Report(reportItems)
  }
}