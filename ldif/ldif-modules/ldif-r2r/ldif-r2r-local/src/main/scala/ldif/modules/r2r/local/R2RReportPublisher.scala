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

package ldif.modules.r2r.local

import collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicInteger
import ldif.util.{JobDetailsStatusMonitor, ReportItem, Report}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RReportPublisher extends JobDetailsStatusMonitor("R2R") {
  var quadsOutput = new AtomicInteger(0)
  var mappingsExecuted = new AtomicInteger(0)
  var mappingsTotal : Int = 0

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    reportItems.append(ReportItem("Mappings", getProgress, mappingsExecuted + " mappings executed<br>" + quadsOutput + " quads output"))
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }
    // reportItems.append(ReportItem.get("mappingsTotal",mappingsTotal))
    Report(reportItems)
  }

  private def getProgress : String =
    (mappingsExecuted.get*100/mappingsTotal).toInt + "%"

  override def getStatus : Option[String] = status.orElse(Some(getProgress))
}