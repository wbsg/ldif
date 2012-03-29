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

package ldif.hadoop

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.util.{CommonUtils, OutputValidator}
import java.io.File
import java.util.Properties
import ldif.config.IntegrationConfig
import ldif.output.SerializingQuadWriter

@RunWith(classOf[JUnitRunner])
class HadoopIntegrationTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.loadFile("integration/integrationJob.xml")

  val fixedProperties = Map(
    ("uriMintNamespace", "http://ldif/"),
    ("uriMintLabelPredicate", "http://source/mintProp"),
    ("provenanceGraphURI", "http://ldif/provGraph"))

  it should "run the whole integration flow correctly (default properties)" in {

    val ldifOutput = runLdif(configFile, CommonUtils.buildProperties(fixedProperties))

    ldifOutput.size should equal (9)
    
    val correctQuads = CommonUtils.getQuads(List(
      "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://www4.wiwiss.fu-berlin.de/ldif/graph#uriRewriting> .  ",
      "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph7> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://www4.wiwiss.fu-berlin.de/ldif/graph#uriRewriting> . ",
      "<http://source/uriC> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://ldif/class> <http://source/graph6> . ",
      "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriA> <http://www4.wiwiss.fu-berlin.de/ldif/graph#generatedBySilk> . ",
      "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph4> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriB> <http://www4.wiwiss.fu-berlin.de/ldif/graph#generatedBySilk> . ",
      "<http://source/uriC> <http://ldif/mapProp> \"map\" <http://source/graph3> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph1> ."
    ))
    OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

    val incorrectQuads = CommonUtils.getQuads(List(
      "<http://source/uriA> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://source/class> ."
    ))
    OutputValidator.containsNot(ldifOutput, incorrectQuads) should equal (true)
  }

  it should "run the whole integration flow correctly (custom properties)" in {
    val properties = CommonUtils.buildProperties(fixedProperties ++ Map(("uriMinting","true"),("useExternalSameAsLinks","false"), ("output","all")))

    val ldifOutput = runLdif(configFile, properties)

    // ldifOutput.size should equal (15)

    val correctQuads = CommonUtils.getQuads(List(
      "<http://ldif/mint> <http://ldif/mapProp> \"map\" <http://source/graph4> .",
      "<http://source/uriB> <http://www.w3.org/2002/07/owl#sameAs> <http://ldif/mint> <http://www4.wiwiss.fu-berlin.de/ldif/graph#uriMinting> ."
    ))
    OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

  }

  //TODO see http://www.assembla.com/spaces/ldif/wiki/Integration_behaviours

  private def runLdif(configFile : File, customProperties : Properties, debugMode: Boolean = false) = {
    var config = IntegrationConfig.load(configFile)
    // override properties
    config = config.copy(properties = customProperties)
    val integrator = new HadoopIntegrationJob(config, debugMode)
    // run integration
    integrator.runIntegration
    CommonUtils.getQuads(new File(integrator.config.outputs.validOutputs.head._1.get.asInstanceOf[SerializingQuadWriter].filepath))
  }
}
