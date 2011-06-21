package ldif.local

import java.io.File
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.fuberlin.wiwiss.ldif.local.EntityBuilderType


@RunWith(classOf[JUnitRunner])
class IntegrationFlowTest extends FlatSpec with ShouldMatchers {

  val configUrl = getClass.getClassLoader.getResource("ldif/local/resources/config.xml")
  val configFile = new File(configUrl.toString.stripPrefix("file:"))

  val config = LdifConfiguration.load(configFile)

  it should "run the whole integration flow correctly" in {

    Main.runIntegrationFlow(configFile)

    val resultsUrl = getClass.getClassLoader.getResource("ldif/local/resources/results.nt")
    val resultsFile = new File(resultsUrl.toString.stripPrefix("file:"))

    OutputValidator.compare(config.outputFile,resultsFile) should equal (0)    
  }
}
