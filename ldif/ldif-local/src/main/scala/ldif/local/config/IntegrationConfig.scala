package ldif.local.config

import java.io.File
import xml.XML
import java.util.Properties
import java.util.logging.Logger

case class IntegrationConfig(sources : File, linkSpecDir : File, mappingDir : File, outputFile : File,  properties : Properties, runSchedule : String) {}

object IntegrationConfig
{
  private val log = Logger.getLogger(getClass.getName)

  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    val propertyFile = new File(baseDir + "/" + (xml \ "properties" text))
    val properties = ConfigProperties.loadProperties(propertyFile)

    var runSchedule : String = (xml \ "runSchedule" text)
    if (runSchedule == "" || runSchedule == null)
      runSchedule = "onStartup"

    IntegrationConfig(
      sources = new File(baseDir + "/" + (xml \ "sources" text)),
      linkSpecDir = new File(baseDir + "/" + (xml \ "linkSpecifications" text)),
      mappingDir = new File(baseDir + "/" + (xml \ "mappings" text)),
      outputFile = new File(xml \ "output" text),
      properties = properties,
      runSchedule = runSchedule
    )
  }
}