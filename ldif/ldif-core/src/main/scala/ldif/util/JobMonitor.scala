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

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */

class JobMonitor extends StatusMonitor with Register[Publisher]{
  def getHtml(params: Map[String, String]) = {
    val sb = new StringBuilder
    sb.append(addHeader("LDIF Job Report", params))
    sb.append("<h1>Status Report for LDIF Jobs</h1>\n")
    sb.append("<table border=\"1\" cellpadding=\"3\" cellspacing=\"0\">")
    sb.append("<tr><th>Job Name</th><th>Start Time</th><th>Finish Time</th><th>Duration</th><th>Status</th><th>Job Infos</th></tr>")
    for((publisher, index) <- getPublishers().zipWithIndex) {
      sb.append("<tr><td>").append(publisher.getPublisherName).append("</td><td>")
        .append(publisher.getFormattedStartTime).append("</td><td>")
      if(publisher.isFinished)
        sb.append(publisher.getFormattedFinishTime)
      else
       sb.append("-")
      sb.append("</td><td>")
      if(publisher.isFinished)
        sb.append(publisher.getDuration).append("s")
      else
       sb.append("-")
      sb.append("</td><td>")
      sb.append(publisher.getStatus.getOrElse("-"))
      sb.append("</td><td>")
      publisher.getLink match {
        case None => sb.append("-")
        case Some(uriPrefix) => sb.append("<a href=\"").append(uriPrefix).append("/").append(index).append("\">details</a>")
      }
      sb.append("</td></tr>\n")
    }
    sb.append("</table></body></html>")
    sb.toString()
  }

  def getText = "Text report not implemented, yet" //TODO
}

object JobMonitor {
  val value: StatusMonitor with Register[Publisher] = new JobMonitor

  def clean() {value.clean()}
}