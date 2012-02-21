package ldif.util

import collection.mutable.{ArrayBuffer, HashMap}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */

trait ReportSubscriber {
  val publishers = new ArrayBuffer[ReportPublisher]

  def addPublisher(reportPublisher: ReportPublisher) {
    publishers.append(reportPublisher)
  }

  def addPublishers(reportPublishers: Seq[ReportPublisher]) {
    for(reportPublisher <- reportPublishers)
      addPublisher(reportPublisher)
  }

  def clean() {
    publishers.clear()
  }
}