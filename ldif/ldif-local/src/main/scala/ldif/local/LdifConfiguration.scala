package ldif.local

import java.io.File
import xml.XML
import collection.mutable.HashSet
import java.net.URL

case class LdifConfiguration(sources : Traversable[String], linkSpecDir : File, mappingFile : File, outputFile : File, propertiesFile: File)
{
  def getLocalSources : Traversable[String] = {
    sources.filterNot(LdifConfiguration.isRemoteSource(_))
  }
}

object LdifConfiguration
{
  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    val propertyFileName = (xml \ "Properties" text)
    var propertyFile: File = null
    if(propertyFileName!="")
      propertyFile = new File(baseDir + "/" + propertyFileName)

    val sourceSet = new HashSet[String]
    // local source directories
    for (sourceDir <- (xml \ "Sources" ).toSeq) {
      var file: File = null
      if(isAbsolutePath(sourceDir.text))
        file = new File(sourceDir.text)
      else
        file = new File(baseDir + "/" + sourceDir.text)

      if(file.isDirectory)
        sourceSet ++= new File(baseDir + "/" + sourceDir.text).listFiles.map(_.getCanonicalPath)
      else
        sourceSet += file.getCanonicalPath
    }

    // local/remote single sources
    for (sourcePath <- (xml \ "Source" ).toSeq)
      sourceSet += sourcePath.text

    LdifConfiguration(
      sources = sourceSet,
      linkSpecDir = new File(baseDir + "/" + (xml \ "LinkSpecifications" text)),
      mappingFile = new File(baseDir + "/" + (xml \ "Mappings" text)),
      outputFile = new File(xml \ "Output" text),
      propertiesFile = propertyFile
    )
  }

  def isAbsolutePath(path: String): Boolean = {
    return (new File(path)).isAbsolute
  }

  def isRemoteSource(sourceLocation: String): Boolean = {
    try {
      val url = new URL(sourceLocation)
      if (url != null && !url.getProtocol.toLowerCase.equals("file"))
        true
      else false
    } catch {
      case e:Exception => {
        false
      }
    }
  }

}