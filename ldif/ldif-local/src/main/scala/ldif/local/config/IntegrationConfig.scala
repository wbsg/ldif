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

    val propertyFile = new File(baseDir + "/" + (xml \ "Properties" text))
    val properties = ConfigProperties.loadProperties(propertyFile)

    var runSchedule : String = (xml \ "RunSchedule" text)
    if (runSchedule == "" || runSchedule == null)
      runSchedule = "onStartup"

    IntegrationConfig(
      sources = new File(baseDir + "/" + (xml \ "Sources" text)),
      linkSpecDir = new File(baseDir + "/" + (xml \ "LinkSpecifications" text)),
      mappingDir = new File(baseDir + "/" + (xml \ "Mappings" text)),
      outputFile = new File(xml \ "Output" text),
      properties = properties,
      runSchedule = runSchedule
    )
  }
}