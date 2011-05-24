package ldif.modules.silk.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.modules.silk.{SilkConfig, SilkModule}
import de.fuberlin.wiwiss.silk.impl.DefaultImplementations
import de.fuberlin.wiwiss.silk.config.{Configuration}
import ldif.util.Prefixes
import ldif.entity.EntityDescription
import xml.XML
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * Unit Test for the SilkLokalExecutor.
 */
//TODO check for more cases
@RunWith(classOf[JUnitRunner])
class SilkLokalExecutorTest extends FlatSpec with ShouldMatchers
{
  DefaultImplementations.register()

  val executor = new SilkLocalExecutor()

  "SilkLokalExecutor" should "return the correct entity descriptions" in
  {
    (executor.input(task).entityDescriptions.head) should equal (entityDescription)
  }

  private lazy val task =
  {
    val configStream = getClass.getClassLoader.getResourceAsStream("ldif/modules/silk/local/PharmGKB.xml")

    val config = Configuration.load(configStream)

    val module = new SilkModule(new SilkConfig(config))

    module.tasks.head
  }

  private lazy val entityDescription =
  {
    implicit val prefixes = Prefixes(task.silkConfig.prefixes)

    val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/silk/local/PharmGKB_EntityDescription.xml")

    EntityDescription.fromXML(XML.load(stream))
  }
}