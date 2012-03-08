package ldif.local

import ldif.util.{StatusMonitor, Register, ReportPublisher, Publisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */

class IntegrationJobStatusMonitor extends Publisher with StatusMonitor with Register[ReportPublisher] {
  def getPublisherName = "Integration Job"

  def getLink: Option[String] = Some("integrationJob")

  def getHtml(params: Map[String, String]) = {
    val sb = new StringBuilder
    sb.append("<html><head><title>Integration Job Report</title>")
    sb.append(addParams(params))
    sb.append("</head><body>\n")
    sb.append("<h1>Status Report for Integration Job</h1>\n")
    for(publisher <- getPublishers()) {
      sb.append("<h3>"+publisher.getPublisherName+"</h2>\n")
      sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">")
      sb.append("<tr><th>report item</th><th>status</th><th>value</th></tr>")
      for(reportItem <- publisher.getReport.items) {
        sb.append("<tr><td>").append(reportItem.name).append("</td><td>")
          .append(reportItem.status).append("</td><td>")
          .append(reportItem.progress).append("</td></tr>\n")
      }
      sb.append("</table>")
    }
    sb.append("</body></html>")
    sb.toString()
  }

  def getText = {
    val sb = new StringBuilder
    sb.append("Status Report for Integration Job: \n\n")
    for(publisher <- getPublishers()) {
      sb.append(publisher.getPublisherName).append(":\n")
      for(reportItem <- publisher.getReport.items) {
        sb.append("    Item: ").append(reportItem.name).append("\n    Status: ")
          .append(reportItem.status).append("\n    Progress: ")
          .append(reportItem.progress).append("\n")
      }
    }
    sb.toString
  }
}