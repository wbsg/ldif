package ldif.local.scheduler

import ldif.util.{ReportPublisher, Register, Publisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/6/12
 * Time: 7:19 PM
 * To change this template use File | Settings | File Templates.
 */

class ImportJobPublisher extends Publisher with Register[ReportPublisher] {
  def getPublisherName = "Import Job"

  def getLink: Option[String] = Some("integrationJob")
}
