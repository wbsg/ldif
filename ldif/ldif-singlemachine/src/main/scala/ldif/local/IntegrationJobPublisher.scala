package ldif.local

import ldif.util.{Register, ReportPublisher, Publisher}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 6:26 PM
 * To change this template use File | Settings | File Templates.
 */

class IntegrationJobPublisher extends Publisher with Register[ReportPublisher] {
  def getPublisherName = "integration job"

  def getLink: Option[String] = Some("integrationJob")
}