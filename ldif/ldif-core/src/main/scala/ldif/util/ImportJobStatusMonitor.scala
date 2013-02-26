package ldif.util

import java.util.concurrent.atomic.AtomicInteger
import collection.mutable.ArrayBuffer

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

class ImportJobStatusMonitor(jobId : Identifier) extends JobDetailsStatusMonitor(jobId) {

  var importedQuads = new AtomicInteger(0)
  var invalidQuads = new AtomicInteger(0)

  var estimatedQuads : Option[Double] = None

  override def getPublisherName = "Import "+jobId

  def getImportedQuads = importedQuads

  def getEstimatedQuadsAsString : String = {
    estimatedQuads match {
      case d:Some[Double] => d.get.intValue.toString
      case None => "No estimation available"
    }
  }

  override def getLink: Option[String] = Some(jobId)

  override def getReport(customReportItems : Seq[ReportItem]) : Report = {
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

  private def getProgress : String =
    if(estimatedQuads!=None && !finished) {
      val progress = (importedQuads.intValue*100/(estimatedQuads.get*1.05)).toInt  // add a 5% margin to the estimation
      if (progress > 99)  // and not finished
        "Loading quads more than expected..."
      else progress +"%"
    }
    else "Loading..."

  override def getStatus : Option[String] =  status.orElse(Some(getProgress))
}