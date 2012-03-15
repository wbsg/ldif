package ldif.util

import java.text.SimpleDateFormat
import java.util.GregorianCalendar

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A report publisher publishes reports about a specific component/publisher
 */
trait ReportPublisher extends Publisher {
  /**
   * Assemble a report to be published
   */
  def getReport: Report

  def getTimeStampReport(name: String): ReportItem = {
    return ReportItem(name, "-", formatDateTime(new GregorianCalendar()))
  }

  def getTimeStampReport(name: String, calendar: GregorianCalendar): ReportItem = {
    return ReportItem(name, "-",formatDateTime(calendar))
  }

  def getDurationTimeReportItem: ReportItem = {
    ReportItem("Duration (seconds)", "-", ""+getDuration)
  }

  def getStartTimeReportItem: ReportItem = getTimeStampReport("Start time", startTime)

  def getFinishTimeReportItem: ReportItem = getTimeStampReport("Finish time", finishTime)

  override def getLink: Option[String] = None
}

case class Report(items: Seq[ReportItem])

case class ReportItem(name: String, status: String, progress: String)