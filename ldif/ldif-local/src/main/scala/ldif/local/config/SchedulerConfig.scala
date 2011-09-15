package ldif.local.config

import java.util.Properties
import java.util.logging.Logger
import java.io.File
import xml.{Elem, XML}

case class SchedulerConfig (importJobsDir : File, integrationJob : File, dataSourcesDir : File, dumpLocationDir : File, properties : Properties)  {}

object SchedulerConfig
{
  private val log = Logger.getLogger(getClass.getName)

  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val properties = ConfigProperties.loadProperties(baseDir + "/" + (xml \ "Properties" text))

    val dumpLocationDir = new File(baseDir + "/" + (xml \ "DumpLocation" text))
    if(!dumpLocationDir.exists && !dumpLocationDir.mkdirs)
      log.severe("Could not create local dump directory at: " + dumpLocationDir.getCanonicalPath)

    val importJobsDir = getDir(xml, "ImportJobs", baseDir)
    val integrationJobDir = getDir(xml, "IntegrationJob", baseDir)
    val datasourceJobDir = getDir(xml, "DataSources", baseDir)

    SchedulerConfig(
      importJobsDir,
      integrationJobDir,
      datasourceJobDir,
      dumpLocationDir,
      properties
    )
  }

  private def getDir (xml : Elem, key : String, baseDir : String) : File = {
    val value : String = (xml \ key text)
    var dir : File = null
    if (value != ""){
      val tmpDir = new File(baseDir + "/" + value)
      if (tmpDir.exists) {
        dir = tmpDir
      }
      else {
        log.warning(key+" path does not exist: "+ dir.getCanonicalPath)
      }
    }
    else{
      log.warning(key+" is not defined in the configuration file")
    }
    dir
  }

}
