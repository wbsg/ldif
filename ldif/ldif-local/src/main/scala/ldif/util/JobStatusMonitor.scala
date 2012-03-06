package ldif.util

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 5:37 PM
 * To change this template use File | Settings | File Templates.
 */

object JobStatusMonitor {
  val value: StatusMonitor with Register[Publisher] = new JobMonitor

  def clean() = value.clean()
}