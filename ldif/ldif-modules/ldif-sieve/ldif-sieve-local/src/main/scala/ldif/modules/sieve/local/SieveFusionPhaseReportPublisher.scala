package ldif.modules.sieve.local

import collection.mutable.ArrayBuffer
import ldif.util.{ReportItem, Report, ReportPublisher, Publisher}
import ldif.util.ReportItem._
import ldif.util.Report._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/8/12
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */

class SieveFusionPhaseReportPublisher extends ReportPublisher {
  def getPublisherName = "Sieve Fusion"

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