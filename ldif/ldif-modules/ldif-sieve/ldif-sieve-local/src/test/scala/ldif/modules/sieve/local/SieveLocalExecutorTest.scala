package ldif.modules.sieve.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.util.Prefixes
import ldif.entity.EntityDescription
import xml.XML
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import ldif.modules.sieve.{SieveConfig, SieveModuleConfig, SieveModule}

/**
 * Unit Test for the SieveLocalExecutor.
 */
//TODO check for more cases
@RunWith(classOf[JUnitRunner])
class SieveLocalExecutorTest extends FlatSpec with ShouldMatchers
{
  //DefaultImplementations.register()

  val executor = new SieveLocalExecutor()

  "SieveLocalExecutor" should "return the correct entity descriptions" in
  {
    (executor.input(task).entityDescriptions.head) should equal (entityDescription)
  }

  private lazy val task =
  {
    val configStream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music.xml")

    val config = SieveConfig.load(configStream)

    val module = new SieveModule(new SieveModuleConfig(config))

    module.tasks.head
  }

  private lazy val entityDescription =
  {
    implicit val prefixes = Prefixes(task.sieveConfig.sieveConfig.prefixes)

    val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music_EntityDescription.xml")
    val testXml = XML.load(stream)
//    val testXml = <EntityDescription>
//      <Patterns>
//        <Pattern>
//          <Path>?a/rdfs:label</Path>
//        </Pattern>
//      </Patterns>
//    </EntityDescription>
    EntityDescription.fromXML(testXml)
  }
}