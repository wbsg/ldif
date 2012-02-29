package ldif.modules.r2r.local

import collection.mutable.ArrayBuffer
import ldif.util.{ReportItem, Report, ReportPublisher}
import ldif.util.ReportItem._
import ldif.util.Report._
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RReportPublisher extends ReportPublisher {
  var quadsOutput = new AtomicInteger(0)
  var mappingsExecuted = new AtomicInteger(0)

  def getPublisherName = "R2R"

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    val status = if(finished) "Done" else "running..."
    reportItems.append(ReportItem("Mappings", status, mappingsExecuted + " mappings executed<br>" + quadsOutput + " quads output"))
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }

    return Report(reportItems)
  }
}