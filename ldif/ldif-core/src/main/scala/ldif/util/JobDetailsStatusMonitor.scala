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

package ldif.util

import collection.mutable.ArrayBuffer

class JobDetailsStatusMonitor(jobId: String) extends Publisher with StatusMonitor with ReportPublisher {

  def getHtml(params: Map[String, String]) = {
    val sb = new StringBuilder
    sb.append(addHeader(jobId+" Job Report", params))
    sb.append("<h1>Status Report for "+jobId+ " Job</h1>\n")
    sb.append("<h3>"+getPublisherName+"</h2>\n")
    sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">")
    sb.append("<tr><th>Report item</th><th>Status</th><th>Value</th></tr>")
    for(reportItem <- getReport.items) {
      sb.append("<tr>")
        .append(buildCell(reportItem.name))
        .append(buildCell(reportItem.status))
        .append(buildCell(reportItem.value))
        .append("</tr>\n")
    }
    sb.append("</table>")
    sb.append("</body></html>")
    sb.toString()
  }

  def getText = {
    val sb = new StringBuilder
    sb.append("Status Report for "+jobId+ " Job: \n\n")
    sb.append(getPublisherName).append(":\n")
    for(reportItem <- getReport.items) {
      sb.append("    Item: ").append(reportItem.name).append("\n    Status: ")
        .append(reportItem.status).append("\n    Progress: ")
        .append(reportItem.value).append("\n")
    }
    sb.toString
  }

  override def getStatus : Option[String] = status

  def getPublisherName = jobId

  override def getLink: Option[String] = Some(jobId)

  def getReport : Report = getReport(Seq.empty[ReportItem])

  def getReport(customReportItems : Seq[ReportItem]) : Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }
    customReportItems.map(reportItems.append(_))
    Report(reportItems)
  }

}