package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 4:20 PM
 * To change this template use File | Settings | File Templates.
 */

class SimpleStatusMonitor extends StatusMonitor with ReportSubscriber {
  def getHtml = null

  def getText = {
    val sb = new StringBuilder
    sb.append("Status Report of Integration Process: \n\n")
    for(publisher <- publishers) {
      sb.append(publisher.getPublisherName).append(":\n")
      for(reportItem <- publisher.getReport.items) {
        sb.append("  ").append(reportItem.name).append(": Status: ")
          .append(reportItem.status).append("; Progress: ")
          .append(reportItem.progress).append("\n")
      }
    }
    sb.toString
  }
}