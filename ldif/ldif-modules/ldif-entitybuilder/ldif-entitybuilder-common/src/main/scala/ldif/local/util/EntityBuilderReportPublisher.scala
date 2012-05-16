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

package ldif.local.util

import collection.mutable.ArrayBuffer
import java.util.concurrent.atomic.AtomicInteger
import ldif.util.{JobDetailsStatusMonitor, Report, ReportItem}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityBuilderReportPublisher extends JobDetailsStatusMonitor("Entity Builder") {
  var dumpsQuads : Double = 0
  var loadedQuads = new AtomicInteger(0)
  var finishedReading = false

  var entityQueuesTotal : Int = -1
  var entityQueuesFilled = new AtomicInteger(0)
  var entitiesTotal = new AtomicInteger(0)
  var entitiesBuilt = new AtomicInteger(0)
  def finishedBuilding = entityQueuesTotal>=0 && entityQueuesTotal==entityQueuesFilled.intValue()

  var ebType : String = ""

  override def getReport: Report = {
    val reportItems = new ArrayBuffer[ReportItem]
    if(ebType!="")
      reportItems.append(ReportItem.get("Type", ebType))
    reportItems.append(ReportItem.get("Loaded quads", getLoadingStatus, loadedQuads))
    reportItems.append(ReportItem.get("Entities queues completed", entityQueuesFilled.get() + "/"+ entityQueuesTotal))
    if(entitiesBuilt.get > 0 || finishedBuilding)
      reportItems.append(ReportItem.get("Entities built", getBuildingStatus, entitiesBuilt +"/"+ entitiesTotal))
    if(dumpsQuads > 0)
      reportItems.append(ReportItem.get("Input Quads",dumpsQuads.toInt))  //TODO only on first
    super.getReport(reportItems)
  }

  def setInputQuads(n : Double) {dumpsQuads = n}
  def getInputQuads = dumpsQuads

  private def getLoadingStatus : String =
    if(dumpsQuads!=0 && !finishedReading) {
      val progress = (loadedQuads.intValue*100/(dumpsQuads)).toInt
      progress +"%"
    }
    else "Done"

  private def getBuildingStatus : String = {
    if(!finishedBuilding) {
      if (entitiesTotal.intValue>0)
        (entitiesBuilt.intValue*100/(entitiesTotal.intValue)).toInt + "%"
      else if(entityQueuesTotal.intValue>=0)
        (entityQueuesFilled.intValue*100/(entityQueuesTotal.intValue)).toInt + "%"
      else
        "Not started yet"
    }
    else "Done"
  }

  override def getStatus : Option[String] = {
    if(finishedBuilding && finishedReading) {
      //in-memory entity building runs in background
      status=Some("Done")
    }
    status.orElse(Some("Loading Quad: "+getLoadingStatus +" <br/> Building Entities: "+ getBuildingStatus))
  }
}