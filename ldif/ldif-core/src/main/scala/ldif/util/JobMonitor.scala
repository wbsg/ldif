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

package ldif.util

class JobMonitor extends StatusMonitor with ReportRegister {
  def getHtml(params: Map[String, String]) = {

    val runningJobs = getRunningJobs
    val completedJobs = getCompleteJobs
    val failedJobs = getFailedJobs

    val sb = new StringBuilder
    sb.append(addHeader("LDIF Job Report", params))
    sb.append("<h1>Status Report for LDIF Jobs</h1>\n")

    //Running tasks
    sb.append(buildTable(runningJobs, "Running Jobs"))

    //Completed tasks
    sb.append(buildTable(completedJobs, "Completed Jobs"))

    //Failed tasks
    if (failedJobs.size>0)
      sb.append(buildTable(failedJobs, "Failed Jobs"))

    sb.append("</body></html>")
    sb.toString()
  }

  def getText = "Text report not implemented, yet" //TODO

  def buildTable (jobs : IndexedSeq[ReportPublisher], caption : String = null) : String = {
    val sb = new StringBuilder
    sb.append("<table  border=\"1\" >")
    if(caption!=null) sb.append("<caption>"+caption+"</caption>")
    sb.append("<tr><th>Job Name</th><th>Status</th><th>Duration</th><th>Start Time</th><th>Job Infos</th></tr>")
    for((publisher, index) <- jobs.zipWithIndex) {
      sb.append("<tr>")
        .append(buildCell(publisher.getPublisherName))
        .append(buildCell(publisher.getStatus.getOrElse("-")))
        .append(buildCell(publisher.getDuration))
        .append(buildCell(publisher.getFormattedStartTime))
        //        .append(buildCell(publisher.getFormattedFinishTime))
        .append(buildCell(publisher.getLinkAsHtml(index)))
        .append("</tr>\n")
    }
    sb.append("</table>")
    sb.toString()
  }
}

object JobMonitor extends JobMonitor