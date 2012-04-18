
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
package ldif.local.report

import java.util.concurrent.atomic.AtomicInteger
import ldif.util.{Report, ReportItem, ReportPublisher}
import collection.mutable.ArrayBuffer

class DumpLoadReportPublisher(val useSameAs: Boolean) extends ReportPublisher {
  var loadedQuads = new AtomicInteger(0)
  var externalSameAsQuads = new AtomicInteger(0)
  var provenanceQuads = new AtomicInteger(0)
  var dumpsQuads : Double = 0

  def getPublisherName = "Dump Loader"

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    reportItems.append(createLoadedQuadsReportItem)
    reportItems.append(createSameAsReportItem)
    reportItems.append(createProvenanceReportItem)
    if (dumpsQuads > 0)
    reportItems.append(ReportItem.get("Quads contained in the dumps",dumpsQuads.toInt))
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }

    Report(reportItems)
  }

  private def createSameAsReportItem: ReportItem = {
    if (useSameAs)
      ReportItem("Nr. of sameAs links extracted", "store", externalSameAsQuads + " quads")
    else
      ReportItem("Nr. of sameAs links extracted", "ignore", "-")
  }

  private def createProvenanceReportItem: ReportItem =
    ReportItem("Nr. of provenance quads extracted", "store", provenanceQuads + " quads")

  private def createLoadedQuadsReportItem: ReportItem =
    ReportItem("Nr. of quads loaded", getStatusAsString, loadedQuads + " quads")

  private def getProgress : Option[String] =
  if(dumpsQuads!=0 && !finished) {
      val progress = (loadedQuads.intValue*100/(dumpsQuads)).toInt
      Some(progress +"%")
    }
    else None

  override def getStatus : Option[String] =  status.orElse(getProgress)

  def setInputQuads(n : Double) {dumpsQuads = n}
  def getInputQuads = dumpsQuads
}