/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.util.{OutputValidator, CommonUtils}
import java.util.Properties
import ldif.config.IntegrationConfig
import ldif.output.SerializingQuadWriter
import java.io.File

@RunWith(classOf[JUnitRunner])
class IntegrationTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.loadFile("integration/integrationJob.xml")

  val fixedProperties = Map(
    ("uriMintNamespace", "http://ldif/"),
    ("uriMintLabelPredicate", "http://source/mintProp"),
    ("provenanceGraphURI", "http://ldif/provGraph"))

  it should "run the whole integration flow correctly (default properties)" in {

    val ldifOutput = runLdif(configFile, CommonUtils.buildProperties(fixedProperties))

    val correctQuads = CommonUtils.getQuads(List(
      "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph7> .  ",
      "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph7> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph6> . ",
      "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph6> . ",
      "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph4> . ",
      "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph4> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph3> . ",
      "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph3> . ",
      "<http://source/graph1> <http://ldif/provProp> \"_\" <http://ldif/provGraph> . "
    ))
    OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

    val incorrectQuads = CommonUtils.getQuads(List(
      "<http://source/uriA> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://source/class> .",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph1> ."
    ))
    OutputValidator.containsNot(ldifOutput, incorrectQuads) should equal(true)
  }

  it should "run the whole integration flow correctly (TDB)" in {
    if(System.getProperty("os.name")=="Linux") {
      val ldifOutput = runLdif(configFile, CommonUtils.buildProperties(fixedProperties ++ Map(("entityBuilderType", "quad-store"))))

      val correctQuads = CommonUtils.getQuads(List(
        "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph7> .  ",
        "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph7> . ",
        "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph6> . ",
        "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph6> . ",
        "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph4> . ",
        "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph4> . ",
        "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph3> . ",
        "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph3> . ",
        "<http://source/graph1> <http://ldif/provProp> \"_\" <http://ldif/provGraph> . "
      ))
      OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

      val incorrectQuads = CommonUtils.getQuads(List(
        "<http://source/uriA> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://source/class> .",
        "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph1> ."
      ))
      OutputValidator.containsNot(ldifOutput, incorrectQuads) should equal(true)
    }
  }

  it should "run the whole integration flow correctly (custom properties)" in {
    val properties = CommonUtils.buildProperties(fixedProperties ++ Map(("uriMinting", "true"), ("useExternalSameAsLinks", "false"), ("output", "all")))

    val ldifOutput = runLdif(configFile, properties)

    ldifOutput.size should equal (17)

    val correctQuads = CommonUtils.getQuads(List(
      "<http://ldif/mint> <http://ldif/mapProp> \"map\" <http://source/graph4> .",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://ldif/mint> <http://source/graph2> ."
    ))
    OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

  }

  it should "run the whole integration flow correctly (custom properties + TDB)" in {
    if(System.getProperty("os.name")=="Linux") {
      val properties = CommonUtils.buildProperties(fixedProperties ++ Map(("entityBuilderType", "quad-store"), ("uriMinting", "true"), ("useExternalSameAsLinks", "false"), ("output", "all")))

      val ldifOutput = runLdif(configFile, properties)

      ldifOutput.size should equal (17)

      val correctQuads = CommonUtils.getQuads(List(
        "<http://ldif/mint> <http://ldif/mapProp> \"map\" <http://source/graph4> .",
        "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://ldif/mint> <http://source/graph2> ."
      ))
      OutputValidator.contains(ldifOutput, correctQuads) should equal(true)
    }
  }

  it should "not output provenance data if configured so" in {
    val properties = CommonUtils.buildProperties(fixedProperties ++ Map(("outputProvenance", "false")))
    val ldifOutput = runLdif(configFile, properties)

    ldifOutput.size should equal (8)
  }

  //TODO see http://www.assembla.com/spaces/ldif/wiki/Integration_behaviours

  private def runLdif(configFile: File, customProperties: Properties, debugMode: Boolean = false) = {
    var config = IntegrationConfig.load(configFile)
    // Override properties
    //  Default values:
    //  - uriMinting=false
    //  - rewriteURIs=true
    //  - output=mapped-only
    //  - useExternalSameAsLinks=true
    config = config.copy(properties = customProperties)
    val integrator = new IntegrationJob(config, debugMode)
    // Run integration
    integrator.runIntegration
    CommonUtils.getQuads(new File(integrator.config.outputs.validOutputs.head._1.get.asInstanceOf[SerializingQuadWriter].filepath))
  }
}
