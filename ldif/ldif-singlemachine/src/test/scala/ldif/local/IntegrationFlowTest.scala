package ldif.local

import java.io.File
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.Node
import ldif.runtime.Quad

@RunWith(classOf[JUnitRunner])
class IntegrationFlowTest extends FlatSpec with ShouldMatchers {

  it should "run the whole integration flow correctly" in {
    // Run LDIF
    val configFile = loadConfig("ldif/local/resources/config.xml")
    val ldifOutput = runLdif(configFile, true)

    // Load results to compare with
    val ldimporterOuputUrl = getClass.getClassLoader.getResource("ldif/local/resources/results.nt")
    val ldimporterOuputFile = new File(ldimporterOuputUrl.toString.stripPrefix("file:"))

    // quantity check
    //Source.fromFile(ldifOutput).getLines.size should equal(4835)
    // quality check
    OutputValidator.compare(ldifOutput,ldimporterOuputFile) should equal (0)
  }

  it should "handle rewriting and provenance correctly" in {
    // Run LDIF
    val configFile = loadConfig("ldif/local/resources/config-provenance.xml")
    val ldifOutput = runLdif(configFile)

    // Create provenance quads to look for
    val provenanceQuads = List(
      Quad(Node.createUriNode("aba"),
        "http://www4.wiwiss.fu-berlin.de/ldif/ldif.owl#lastmod",
        Node.createTypedLiteral("2011-01-01T01:00:00.000+01:00","http://www.w3.org/2001/XMLSchema#dateTime"),
        "http://www4.wiwiss.fu-berlin.de/ldif/provenance"),
      Quad(Node.createUriNode("http://brain-map.org/mouse/brain/Chrna7.xml"),
        "http://mywiki/resource/property/MgiMarkerAccessionId",
        Node.createTypedLiteral("MGI:99779","http://www.w3.org/2001/XMLSchema#string"),
        "aba"))

    // quantity check
    //Source.fromFile(ldifOutput).getLines.size should equal(15205)
    // quality check
    OutputValidator.contains(ldifOutput,provenanceQuads) should equal (true)
  }

  it should "be correct with TDB backend" in {
// Run LDIF
    val configFile = loadConfig("ldif/local/resources/config-tdb.xml")
    val ldifOutput = runLdif(configFile)

    // Load results to compare with
    val ldimporterOuputUrl = getClass.getClassLoader.getResource("ldif/local/resources/results.nt")
    val ldimporterOuputFile = new File(ldimporterOuputUrl.toString.stripPrefix("file:"))

    // quality check
    OutputValidator.compare(ldifOutput,ldimporterOuputFile) should equal (0)
  }


  protected def loadConfig(config : String) =  {

    val configUrl = getClass.getClassLoader.getResource(config)
    new File(configUrl.toString.stripPrefix("file:"))
  }

  protected def runLdif(configFile : File, debugMode: Boolean = false) = {

    Main.runIntegrationFlow(configFile, debugMode)
    LdifConfiguration.load(configFile).outputFile
  }
}
