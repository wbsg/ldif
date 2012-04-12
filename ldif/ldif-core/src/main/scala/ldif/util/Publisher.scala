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

import java.text.SimpleDateFormat
import java.util.{Calendar, GregorianCalendar}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A publisher for LDIF events. Has the most basic methods.
 */
trait Publisher {
  protected[this] var startTime = new GregorianCalendar()
  protected[this] var finishTime = new GregorianCalendar()
  protected[this] var finished = false
  protected[this] var status : Option[String] = None

  /**
   * The name of the publisher (should be globally unique)
   */
  def getPublisherName: String

  def setStartTime() {startTime = new GregorianCalendar()}

  def setFinishTime() {
    finishTime = new GregorianCalendar()
    finished = true
    status = Some("Done")
  }

  def formatDateTime(calendar: Calendar) : String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    dateFormat.format(calendar.getTime)
  }

  def setFailed = status = Some("Failed")

  def setSkipped() {
    status = Some("Skipped")
  }

  def isFinished = finished

  def getFormattedStartTime = formatDateTime(startTime)

  def getFormattedFinishTime = if(finished) formatDateTime(finishTime) else "-"

  /**
   * This should return the URI prefix that a link would have, if there is one
   */
  def getLink: Option[String]

  def getLinkAsHtml(index : Int) = getLink match {
    case Some(uriPrefix) => "<a href=\""+uriPrefix+"/"+index+"\">details</a>"
    case None => ""
  }

  /**
   * The duration of the job in seconds as string representation
   */
  def getDuration: String = if (finished) (finishTime.getTime.getTime - startTime.getTime.getTime)/1000.0+" s"  else ""

  /**
   * The status of the job 
   */
  def getStatus : Option[String] = status

  def getStatusAsString = getStatus match {
    case Some(s) => s
    case None => ""
  }

  def setStatus(status : String) {this.status = Some(status)}
}