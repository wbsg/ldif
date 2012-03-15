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

  /**
   * The name of the publisher (should be globally unique)
   */
  def getPublisherName: String

  def setStartTime() = startTime = new GregorianCalendar()

  def setFinishTime() = {
    finishTime = new GregorianCalendar()
    finished = true
  }

  def formatDateTime(calendar: Calendar): String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    return dateFormat.format(calendar.getTime)
  }

  def isFinished = finished

  def getFormattedStartTime = formatDateTime(startTime)

  def getFormattedFinishTime = formatDateTime(finishTime)

  /**
   * This should return the URI prefix that a link would have, if there is one
   */
  def getLink: Option[String]

  /**
   * The duration of the job in seconds as string representation
   */
  def getDuration: String = ""+(finishTime.getTime.getTime - startTime.getTime.getTime)/1000.0
}