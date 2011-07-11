package ldif.local

import java.io.File
import xml.XML
import collection.mutable.HashSet

case class LdifConfiguration(sources : Traversable[String], linkSpecDir : File, mappingFile : File, outputFile : File, propertiesFile: File)

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
    for (sourceDir <- (xml \ "Sources" ).toSeq)
      sourceSet ++= new File(baseDir + "/" + sourceDir.text).listFiles.map(_.getCanonicalPath)

    // remote sources
    for (sourceUrl <- (xml \ "RemoteSources" \ "Source" ).toSeq)
      sourceSet += sourceUrl.text

    LdifConfiguration(
      sources = sourceSet,
      linkSpecDir = new File(baseDir + "/" + (xml \ "LinkSpecifications" text)),
      mappingFile = new File(baseDir + "/" + (xml \ "Mappings" text)),
      outputFile = new File(xml \ "Output" text),
      propertiesFile = propertyFile
    )
  }
}