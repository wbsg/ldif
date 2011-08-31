package ldif.config

import xml.XML
import java.io.{IOException, FileInputStream, BufferedInputStream, File}
import java.util.Properties
import java.util.logging.Logger

case class SchedulerConfig (importJobsDir : File, dataSourcesDir : File, properties : Properties)  {
  def getLastUpdateProperty = "http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"
}

object SchedulerConfig
{
  private val log = Logger.getLogger(getClass.getName)

  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val properties = new Properties
    val propertyFileName = (xml \ "Properties" text)
    if (propertyFileName!="") {

      val propertyFile = new File(baseDir + "/" + propertyFileName)
      if(propertyFile.exists())

        try {

          val stream = new BufferedInputStream(new FileInputStream(propertyFile))
          properties.load(stream)
          stream.close()

        } catch {

          case e: IOException => {
            log.severe("No property file found at: " + propertyFile.getAbsoluteFile)
            System.exit(1)
          }
        }
    }

    SchedulerConfig(
      new File(baseDir + "/" + (xml \ "ImportJobs" text)),
      new File(baseDir + "/" + (xml \ "DataSources" text)),
      properties
    )
  }
}
