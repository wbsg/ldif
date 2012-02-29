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
trait ReportPublisher {
  private var startTime = new GregorianCalendar()//getTimeStampReport("Start time")
  private var finishTime = new GregorianCalendar()//: ReportItem = ReportItem("", "", "")
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
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    val time = dateFormat.format(new GregorianCalendar().getTime)
    return ReportItem(name, "-",time)
  }

  def getTimeStampReport(name: String, calendar: GregorianCalendar): ReportItem = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    val time = dateFormat.format(calendar.getTime)
    return ReportItem(name, "-",time)
  }

  def setStartTime = startTime = new GregorianCalendar() //getTimeStampReport("Start time")

  def setFinishTime = {
    finishTime = new GregorianCalendar()//getTimeStampReport("Finish time")
    finished = true
  }

  def getDurationTimeReportItem: ReportItem = {
    val difference = finishTime.getTime.getTime - startTime.getTime.getTime
    ReportItem("Duration (seconds)", "-", ""+difference/1000.0)
  }

  def getStartTimeReportItem: ReportItem = getTimeStampReport("Start time", startTime)

  def getFinishTimeReportItem: ReportItem = getTimeStampReport("Finish time", finishTime)
}

case class Report(items: Seq[ReportItem])

case class ReportItem(name: String, status: String, progress: String)