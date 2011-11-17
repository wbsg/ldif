package ldif.local.config

import java.util.logging.Logger
import java.io.File
import ldif.util.ValidatingXMLReader
import xml.{Node, XML}
import java.util.Properties

case class SchedulerConfig (importJobsDir : File, integrationJob : File, dataSourcesDir : File, dumpLocationDir : File, properties : Properties)  {}

object SchedulerConfig
{
  private val log = Logger.getLogger(getClass.getName)

  private val schemaLocation = "xsd/SchedulerConfig.xsd"

  def empty = SchedulerConfig(null, null, null, null, new Properties)

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File) = {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val propertiesFile = getFile(xml, "properties", baseDir)
    var properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    val dumpLocationDir = getFile(xml, "dumpLocation", baseDir, true)

    val importJobsDir = getFile(xml, "importJobs", baseDir)
    val integrationJobDir = getFile(xml, "integrationJob", baseDir)
    val datasourceJobDir = getFile(xml, "dataSources", baseDir)

    SchedulerConfig(
      importJobsDir,
      integrationJobDir,
      datasourceJobDir,
      dumpLocationDir,
      properties
    )
  }

  private def getFile (xml : Node, key : String, baseDir : String, forceMkdir : Boolean = false) : File = {
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
        if (forceMkdir) {
          if (relativeFile.mkdirs) {
            file = relativeFile
            log.info("Created new directory at: "+ relativeFile.getCanonicalPath)
          }
          else log.severe("Error creating directory at: " + relativeFile.getCanonicalPath)
        }
      }
    }
    else{
      log.warning("\'"+key+"\' is not defined in the Scheduler config")
    }
    file
  }

}
