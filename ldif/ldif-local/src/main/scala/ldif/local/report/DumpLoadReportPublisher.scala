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

package ldif.local.report

import java.util.concurrent.atomic.AtomicInteger
import collection.mutable.ArrayBuffer
import ldif.util.{JobDetailsStatusMonitor, Report, ReportItem}

class DumpLoadReportPublisher(val useSameAs: Boolean) extends JobDetailsStatusMonitor("Dump Loader") {
  var loadedQuads = new AtomicInteger(0)
  var externalSameAsQuads = new AtomicInteger(0)
  var provenanceQuads = new AtomicInteger(0)
  var dumpsQuads : Double = 0

  override def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(createLoadedQuadsReportItem)
    reportItems.append(createSameAsReportItem)
    reportItems.append(createProvenanceReportItem)
    if (dumpsQuads > 0) {
      reportItems.append(ReportItem.get("Quads contained in the dumps", dumpsQuads.toInt))
    }
    super.getReport(reportItems)
  }

  private def createSameAsReportItem: ReportItem = {
    if (useSameAs) {
      ReportItem.get("Nr. of sameAs links extracted", "store", externalSameAsQuads)
    } else {
      ReportItem.get("Nr. of sameAs links extracted", "ignore", "-")
    }
  }

  private def createProvenanceReportItem: ReportItem = {
    ReportItem.get("Nr. of provenance quads extracted", "store", provenanceQuads)
  }

  private def createLoadedQuadsReportItem: ReportItem = {
    ReportItem.get("Nr. of quads loaded", getStatusAsString, loadedQuads)
  }

  private def getProgress : String = {
    if(dumpsQuads!=0 && !finished) {
      val progress = (loadedQuads.intValue*100/(dumpsQuads)).toInt
      progress +"%"
    } else {
      "Loading..."
    }
  }

  override def getStatus : Option[String] = status.orElse(Some(getProgress))

  def setInputQuads(n : Double) {dumpsQuads = n}

  def getInputQuads = dumpsQuads
}