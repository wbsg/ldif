package ldif.util

import java.util.GregorianCalendar

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


  def setStartTime = startTime = new GregorianCalendar()

  def setFinishTime = {
    finishTime = new GregorianCalendar()
    finished = true
  }
}