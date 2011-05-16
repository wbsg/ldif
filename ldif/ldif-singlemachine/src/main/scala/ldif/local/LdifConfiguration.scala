package ldif.local

import java.io.File
import xml.XML

case class LdifConfiguration(sourceDir : File, linkSpecDir : File, outputFile : File)

object LdifConfiguration
{
  def load(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    LdifConfiguration(
      sourceDir = new File(baseDir + "/" + (xml \ "Sources" text)),
      linkSpecDir = new File(baseDir + "/" + (xml \ "LinkSpecifications" text)),
      outputFile = new File(baseDir + "/" + (xml \ "Output" text))
    )
  }
}