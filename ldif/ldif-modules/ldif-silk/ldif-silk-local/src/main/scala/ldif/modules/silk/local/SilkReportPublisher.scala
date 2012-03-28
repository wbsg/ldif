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

package ldif.modules.silk.local

import collection.mutable.ArrayBuffer
import ldif.util.{ReportItem, Report, ReportPublisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/8/12
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */

class SilkReportPublisher extends ReportPublisher {
  def getPublisherName = "Silk"

  def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    reportItems.append(getStartTimeReportItem)
    if(finished) {
      reportItems.append(getFinishTimeReportItem)
      reportItems.append(getDurationTimeReportItem)
    }

    return Report(reportItems)
  }
}