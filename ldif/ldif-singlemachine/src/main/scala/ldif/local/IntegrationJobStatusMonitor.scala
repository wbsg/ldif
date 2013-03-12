/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local

import ldif.util._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 6:26 PM
 */

class IntegrationJobStatusMonitor extends Publisher with StatusMonitor with ReportRegister {

  override def addPublisher(publisher: ReportPublisher) {
     JobMonitor.addPublisher(publisher)
     super.addPublisher(publisher)
  }

  def getPublisherName = "Integration Job"

  def getLink: Option[String] = Some("integrationJob")

  def getHtml(params: Map[String, String]) = {
    val sb = new StringBuilder
    sb.append(addHeader("Integration Job Report", params))
    sb.append("<h1>Status Report for Integration Job</h1>\n")
    for(publisher <- getPublishers()) {
      sb.append("<h3>"+publisher.getPublisherName+"</h2>\n")
      sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">")
      sb.append("<tr><th>report item</th><th>status</th><th>value</th></tr>")
      for(reportItem <- publisher.getReport.items) {
        sb.append("<tr>")
          .append(buildCell(reportItem.name))
          .append(buildCell(reportItem.status))
          .append(buildCell(reportItem.value))
          .append("</tr>\n")
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
          .append(reportItem.value).append("\n")
      }
    }
    sb.toString()
  }

  override def getStatus : Option[String] = status
}