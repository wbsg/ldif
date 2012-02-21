package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 3:53 PM
 * To change this template use File | Settings | File Templates.
 */

trait ReportPublisher {

  def getPublisherName: String

  def getReport: Report
}

case class Report(items: Seq[ReportItem])

case class ReportItem(name: String, status: String, progress: String)