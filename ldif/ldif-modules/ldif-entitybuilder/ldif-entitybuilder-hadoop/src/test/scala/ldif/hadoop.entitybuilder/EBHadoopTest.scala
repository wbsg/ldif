package ldif.hadoop

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import ldif.hadoop.entitybuilder.phases._
import ldif.entity.{EntityDescription, EntityDescriptionMetaDataExtractor}
import java.io.File
import xml.{XML, Source}
import ldif.util.{Consts, Prefixes}

/**
 * Unit Test for the Hadoop Entity Builder Module.
 */

@RunWith(classOf[JUnitRunner])
class EBHadoopTest extends FlatSpec with ShouldMatchers
{
  val resourceDir = getClass.getClassLoader.getResource("hadoop").getPath.toString
  val sourcesDir = resourceDir+Consts.fileSeparator+"sources"
  val ouputDirPrefix = resourceDir+Consts.fileSeparator+"output_phase"
  val edDir = resourceDir+Consts.fileSeparator+"entity_descriptions"

  val entityDescriptions = loadEDs(edDir)
  val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

  Phase2.runPhase(sourcesDir, ouputDirPrefix+"_2", edmd)
  Phase3.runPhase(ouputDirPrefix+"_2", ouputDirPrefix+"_3", edmd)
  Phase4.runPhase(ouputDirPrefix+"_3", ouputDirPrefix+"_4", edmd)


  "EBHadoop" should "create the correct number of entities" in  {
    //TODO    .. should equal (2)
  }

  // Utils

  private def loadEDs(sourceDir : String) : Seq[EntityDescription] = {
    val dir = new File(sourceDir)
    var eds = Seq.empty[EntityDescription]
    for (file <- dir.listFiles) {
      if (file.isDirectory)
        eds ++= loadEDs(file.getCanonicalPath)
      else eds :+= loadED(file.getCanonicalPath)
    }
    eds
  }

  private def loadED(sourcePath : String) : EntityDescription = {
    implicit val prefixes = Prefixes(
      Map("rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
    val stream =  Source.fromFile(sourcePath).getByteStream
    EntityDescription.fromXML(XML.load(stream))
  }
}