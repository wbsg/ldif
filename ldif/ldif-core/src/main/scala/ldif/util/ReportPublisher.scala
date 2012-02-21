package ldif.util

import java.util.GregorianCalendar
import java.text.SimpleDateFormat

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
trait ReportPublisher {
  var startTime = getTimeStampReport("Start time")
  var finishTime: ReportItem = ReportItem("", "", "")
  var finished = false
  /**
   * The name of the publisher (should be globally unique)
   */
  def getPublisherName: String

  /**
   * Assemble a report to be published
   */
  def getReport: Report

  def getTimeStampReport(name: String): ReportItem = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val time = dateFormat.format(new GregorianCalendar().getTime)
    return ReportItem(name, "-",time)
  }

  def setStartTime = startTime = getTimeStampReport("Start time")

  def setFinishTime = {
    finishTime = getTimeStampReport("Finish time")
    finished = true
  }
}

case class Report(items: Seq[ReportItem])

case class ReportItem(name: String, status: String, progress: String)