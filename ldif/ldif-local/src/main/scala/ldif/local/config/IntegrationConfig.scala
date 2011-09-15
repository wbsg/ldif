package ldif.local.config

import java.io.File
import xml.XML
import collection.mutable.HashSet
import java.util.Properties
import java.util.logging.Logger
import java.net.URL

case class IntegrationConfig(sources : File, linkSpecDir : File, mappingDir : File, outputFile : File,  properties : Properties, runSchedule : String) {

//  // TODO: deprecated - LDIF_0.3 only supports local sources
//  def getLocalSources = sources.filterNot(isRemoteSource(_))
//
//  // TODO: deprecated - LDIF_0.3 only supports local sources
//  def isRemoteSource(sourceLocation: String): Boolean = {
//    try {
//      val url = new URL(sourceLocation)
//      if (url != null && !url.getProtocol.toLowerCase.equals("file"))
//        true
//      else false
//    } catch {
//      case e:Exception => {
//        false
//      }
//    }
//  }

}

object IntegrationConfig
{
  private val log = Logger.getLogger(getClass.getName)

  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    val propertyFile = new File(baseDir + "/" + (xml \ "Properties" text))
    val properties = ConfigProperties.loadProperties(propertyFile)

//    val sourceSet = new HashSet[String]
//    // source directories
//    for (sourceDir <- (xml \ "Sources" ).toSeq) {
//      var file: File = null
//      if(isAbsolutePath(sourceDir.text))
//        file = new File(sourceDir.text)
//      else
//        file = new File(baseDir + "/" + sourceDir.text)
//
//      if(file.isDirectory) {
//        if(file.isAbsolute)
//          sourceSet ++= new File(file.getCanonicalPath).listFiles.map(_.getCanonicalPath)
//        else
//          sourceSet ++= new File(baseDir + "/" + sourceDir.text).listFiles.map(_.getCanonicalPath)
//      }
//      else
//        sourceSet += file.getCanonicalPath
//    }
//
//    // single sources
//    for (sourcePath <- (xml \ "Source" ).toSeq)
//      sourceSet += sourcePath.text

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

//  def isAbsolutePath(path: String): Boolean = {
//    return (new File(path)).isAbsolute
//  }

}