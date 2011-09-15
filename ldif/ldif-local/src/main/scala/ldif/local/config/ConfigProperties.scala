package ldif.local.config

import java.util.logging.Logger
import java.util.Properties
import java.io.{IOException, FileInputStream, BufferedInputStream, File}

object ConfigProperties {
  private val log = Logger.getLogger(getClass.getName)

  def loadProperties(propertyPath : String) : Properties = {
    loadProperties(new File(propertyPath))
  }

  def loadProperties(propertyFile : File): Properties = {

    val properties = new Properties
    if(propertyFile.exists) {

      try {
        val stream = new BufferedInputStream(new FileInputStream(propertyFile))
        properties.load(stream)
        stream.close

      } catch {
        case e: IOException => {
          log.severe("Error reading the properties file at: " + propertyFile.getCanonicalPath)
        }
      }
    }
    else
      log.severe("No properties file found at: " + propertyFile.getCanonicalPath)

    properties
  }

}