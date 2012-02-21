package ldif.util

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
  /**
   * Initialize the ReportPublisher
   */
  def initialize

  /**
   * The name of the publisher (should be globally unique)
   */
  def getPublisherName: String

  /**
   * Assemble a report to be published
   */
  def getReport: Report
}

case class Report(items: Seq[ReportItem])

case class ReportItem(name: String, status: String, progress: String)