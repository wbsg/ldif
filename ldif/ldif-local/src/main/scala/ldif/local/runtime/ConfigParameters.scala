package ldif.local.runtime

import java.util.logging.Logger
import java.util.Properties
import java.io.{IOException, FileInputStream, BufferedInputStream, File}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 21.06.11
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */

case class ConfigParameters(val configProperties: ConfigProperties, val otherQuadsWriter: QuadWriter)


class ConfigProperties  {
  var properties: Properties = new Properties()

  def loadPropertyFile(propertyFile: File) {
    // do nothing
  }

  def getPropertyValue(property: String): String = {
    null
  }

  def getPropertyValue(property: String, default: String): String = {
    default
  }

}