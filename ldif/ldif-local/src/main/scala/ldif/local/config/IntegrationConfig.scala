package ldif.local.config

import java.io.File
import java.util.Properties
import java.util.logging.Logger
import ldif.util.ValidatingXMLReader
import xml.{Node, XML}
import com.hp.hpl.jena.xmloutput.impl.Abbreviated

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
      outputFile = new File(xml \ "output" text),
      properties = properties,
      runSchedule = runSchedule
    )
  }

  private def getFile (xml : Node, key : String, baseDir : String) : File = {
    val value : String = (xml \ key text)
    var file : File = null
    if (value != ""){
      val relativeFile = new File(baseDir + "/" + value)
      val absoluteFile = new File(value)
      if (relativeFile.exists || absoluteFile.exists) {
        if (relativeFile.exists)
          file = relativeFile
        else file = absoluteFile
      }
      else {
        log.warning("\'"+key+"\' path not found. Searched: " + relativeFile.getCanonicalPath + ", " + absoluteFile.getCanonicalPath)
      }
    }
    else{
      log.warning("\'"+key+"\' is not defined in the IntegrationJob config")
    }
    file
  }
}