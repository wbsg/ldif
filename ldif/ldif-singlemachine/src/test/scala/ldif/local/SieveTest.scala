/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.util.{OutputValidator, CommonUtils}
import java.io.File
import ldif.config.IntegrationConfig
import ldif.output.SerializingQuadWriter


@RunWith(classOf[JUnitRunner])
class SieveTest extends FlatSpec with ShouldMatchers {

  //TODO Fix -these tests can be moved into IntegrationTest
  val configFile = CommonUtils.loadFile("sieve/integrationJob.xml")

  it should "sieve works correctly" in {
    val ldifOutput = runLdif(configFile)


    //ldifOutput.size should equal (6)

    println(ldifOutput.size.toString)
    for (q <- ldifOutput) println(q.toLine)

    val correctQuads = CommonUtils.getQuads(List(
      //TODO Fix - all these quads should be included
      //"<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/areaTotal> \"7.12E8\"^^<http://www.w3.org/2001/XMLSchema#double> <http://en.wikipedia.org/wiki/Ubatuba> . ",
      //"<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/areaTotal> \"3.3669845434368E8\"^^<http://www.w3.org/2001/XMLSchema#double> <http://en.wikipedia.org/wiki/Ubatuba> .",
      "<http://dbpedia.org/resource/Tombos> <http://dbpedia.org/ontology/areaTotal> \"2.83483E11\"^^<http://www.w3.org/2001/XMLSchema#double> <http://pt.wikipedia.org/wiki/Tombos> .",
      "<http://dbpedia.org/resource/Tombos> <http://dbpedia.org/ontology/populationTotal> \"9542\"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> <http://pt.wikipedia.org/wiki/Tombos> ." ,
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/populationTotal> \"81246\"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> <http://en.wikipedia.org/wiki/Ubatuba> .",
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/foundingDate> \"1637-10-28\"^^<http://www.w3.org/2001/XMLSchema#date> <http://en.wikipedia.org/wiki/Ubatuba> ."
    ))
    OutputValidator.contains(ldifOutput, correctQuads) should equal(true)

    val incorrectQuads = CommonUtils.getQuads(List(
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/areaTotal> \"7.12116E11\"^^<http://www.w3.org/2001/XMLSchema#double> <http://pt.wikipedia.org/wiki/Ubatuba>. "
    ))
    OutputValidator.containsNot(ldifOutput, incorrectQuads) should equal(true)


  }

  private def runLdif(configFile: File, debugMode: Boolean = false) = {
    var config = IntegrationConfig.load(configFile)
    val integrator = new IntegrationJob(config, debugMode)
    // Run integration
    integrator.runIntegration
    CommonUtils.getQuads(new File(integrator.config.outputs.outputs.head._1.asInstanceOf[SerializingQuadWriter].filepath))
  }

}