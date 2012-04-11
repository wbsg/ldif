package ldif.util

import java.util.concurrent.atomic.AtomicInteger
import collection.mutable.ArrayBuffer

/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

class ImportJobStatusMonitor(jobId : Identifier) extends Publisher with StatusMonitor with ReportPublisher {

  var importedQuads = new AtomicInteger(0)
  var invalidQuads = new AtomicInteger(0)

  var estimatedQuads : Option[Double] = None

  def getImportedQuads = importedQuads

  def getEstimatedQuadsAsString : String = {
    estimatedQuads match {
      case d:Some[Double] => d.get.intValue.toString
      case None => "No estimation available"
    }
  }

  def getPublisherName = "Import Job "+ jobId

  override def getLink: Option[String] = Some(jobId)

  def getHtml(params: Map[String, String]) = {
    val sb = new StringBuilder
    sb.append(addHeader("Import Job Report", params))
    sb.append("<h1>Status Report for Import Job</h1>\n")
    sb.append("<h3>"+getPublisherName+"</h2>\n")
    sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">")
    sb.append("<tr><th>report item</th><th>status</th><th>value</th></tr>")
    for(reportItem <- getReport.items) {
      sb.append("<tr>")
      sb.append(buildCell(reportItem.name))
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
    sb.append("Status Report for Import Job: \n\n")
    sb.append(getPublisherName).append(":\n")
    for(reportItem <- getReport.items) {
      sb.append("    Item: ").append(reportItem.name).append("\n    Status: ")
        .append(reportItem.status).append("\n    Progress: ")
        .append(reportItem.value).append("\n")
    }
    sb.toString
  }

  def getReport : Report = getReport(Seq.empty[ReportItem])

  def getReport(customReportItems : Seq[ReportItem] = Seq.empty[ReportItem]) : Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }
    reportItems.append(ReportItem.get("Loaded Quads", getStatusAsString, importedQuads))
    if (invalidQuads.get > 0)
      reportItems.append(ReportItem.get("Invalid Quads", invalidQuads))

    reportItems.append(ReportItem.get("Estimated number of Quads",getEstimatedQuadsAsString))

    customReportItems.map(reportItems.append(_))

    Report(reportItems)
  }

  private def getProgress : Option[String] =
    if(estimatedQuads!=None && !finished) {
      val progress = (importedQuads.intValue*100/(estimatedQuads.get*1.05)).toInt  // add a 5% margin to the estimation
      if (progress > 99)  // and not finished
        Some("loading quads more than expected...")
      else Some(progress +" %")
    }
    else None

  override def getStatus : Option[String] =  status.orElse(getProgress)
}