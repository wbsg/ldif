package ldif.local

import ldif.util.{ReportPublisher, StatusMonitor, Publisher, Register}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 6:45 PM
 * To change this template use File | Settings | File Templates.
 */

object IntegrationJobStatusMonitor {
  private[local] var value: Register[ReportPublisher] = new Register[ReportPublisher] {}

  def getIntegrationJobPublisher = value
}