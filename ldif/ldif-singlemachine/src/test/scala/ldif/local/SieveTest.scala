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

  //TODO These tests can be moved into IntegrationTest
  val configFile = CommonUtils.loadFile("sieve/integrationJob.xml")

  it should "sieve works correctly" in {
    val ldifOutput = runLdif(configFile)

//    println(ldifOutput.size.toString)
//    for (q <- ldifOutput) print(q.toLine)

    ldifOutput.size should equal (13)

    val correctQuads = CommonUtils.getQuads(List(
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/areaTotal> \"7.12E8\"^^<http://www.w3.org/2001/XMLSchema#double> <http://en.wikipedia.org/wiki/Ubatuba> . ",
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/areaTotal> \"3.3669845434368E8\"^^<http://www.w3.org/2001/XMLSchema#double> <http://en.wikipedia.org/wiki/Ubatuba> .",
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/populationTotal> \"81246\"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> <http://en.wikipedia.org/wiki/Ubatuba> .",
      "<http://dbpedia.org/resource/Ubatuba> <http://dbpedia.org/ontology/foundingDate> \"1637-10-28\"^^<http://www.w3.org/2001/XMLSchema#date> <http://en.wikipedia.org/wiki/Ubatuba> .",
      "<http://dbpedia.org/resource/Ubatuba> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/City> <http://pt.wikipedia.org/wiki/Ubatuba>.",
      "<http://en.wikipedia.org/wiki/Ubatuba> <http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate> \"2011-07-26T18:49:34Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <http://www4.wiwiss.fu-berlin.de/ldif/provenance>.",
      "<http://pt.wikipedia.org/wiki/Ubatuba> <http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate> \"2010-07-26T18:52:08Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <http://www4.wiwiss.fu-berlin.de/ldif/provenance>.",
      "<http://en.wikipedia.org/wiki/Ubatuba> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph> <http://www4.wiwiss.fu-berlin.de/ldif/provenance>.",
      "<http://pt.wikipedia.org/wiki/Ubatuba> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph> <http://www4.wiwiss.fu-berlin.de/ldif/provenance>.",
      "<http://dbpedia.org/resource/Recife> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/City> <http://pt.wikipedia.org/wiki/Recife>.",
      "<http://dbpedia.org/resource/Brazil> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Country> <http://pt.wikipedia.org/wiki/Brazil>.",
      "<http://dbpedia.org/resource/Brazil> <http://dbpedia.org/ontology/capital> <http://dbpedia.org/resource/Brasilia> <http://en.wikipedia.org/wiki/Brazil>." ,
      "<http://dbpedia.org/resource/Brazil> <http://dbpedia.org/ontology/areaTotal> \"8.51E9\"^^<http://www.w3.org/2001/XMLSchema#double> <http://en.wikipedia.org/wiki/Brazil>."
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