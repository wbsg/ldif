package ldif.local.config

import java.io.File
import java.util.Properties
import java.util.logging.Logger
import ldif.util.ValidatingXMLReader
import xml.{Node, XML}

case class IntegrationConfig(sources : File, linkSpecDir : File, mappingDir : File, outputFile : File,  properties : Properties, runSchedule : String) {}

object IntegrationConfig
{
  private val log = Logger.getLogger(getClass.getName)

  private val schemaLocation = "xsd/IntegrationJob.xsd"

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val propertiesFile = getFile(xml, "properties", baseDir)
    var properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    var runSchedule : String = (xml \ "runSchedule" text)
    if (runSchedule == "" || runSchedule == null)
      runSchedule = "onStartup"

    IntegrationConfig(
      sources = getFile(xml, "sources", baseDir),
      linkSpecDir = getFile(xml, "linkSpecifications", baseDir),
      mappingDir = getFile(xml, "mappings", baseDir),
      outputFile = getFile(xml, "output"),
      properties = properties,
      runSchedule = runSchedule
    )
  }

  private def getFile (xml : Node, key : String, baseDir : String = null) : File = {
    val value : String = (xml \ key text)
    var file : File = null
    if (value != ""){
      var tmpFilePath = value
      if (baseDir != null)
        tmpFilePath = baseDir + "/" + tmpFilePath
      val tmpFile = new File(tmpFilePath)
      if (tmpFile.exists) {
        file = tmpFile
      }
      else {
        log.warning("\'"+key+"\' path not found: "+ tmpFile.getCanonicalPath)
      }
    }
    else{
      log.warning("\'"+key+"\' is not defined in the configuration file")
    }
    file
  }
}