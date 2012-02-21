package ldif.util


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */

object GlobalStatusMonitor {
  val value: StatusMonitor with ReportSubscriber = new SimpleStatusMonitor
}