/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.config

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers
import ldif.util.CommonUtils
import ldif.output.{SerializingQuadWriter, SparqlWriter}

@RunWith(classOf[JUnitRunner])
class ConfigTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.getFileFromPath("ldif/config/integrationJob.xml")
  val config = IntegrationConfig.fromFile(configFile)

  // Sources

  it should "parse Sources correctly" in {
    config.sources.head should endWith ("dumps")
  }

  // Outputs

  it should "parse Sparql output config correctly" in {
    val xml = {<sparql>
        <endpointURI>http://host/sparql</endpointURI>
        <queryParameter>update</queryParameter>
        <sparqlVersion>1.1</sparqlVersion>
        <useDirectPost>false</useDirectPost>
        <user>usr</user>
        <password>pwd</password>
      </sparql>}
    // build SparqlWriter but do not validate
    val writer = SparqlWriter.fromXML(xml, false).get
    writer.uri should equal ("http://host/sparql")
    writer.login should equal (Some("usr","pwd"))
    writer.queryParameter should equal ("update")
    writer.useDirectPost should equal (false)
    writer.sparqlVersion should equal ("1.1")
  }

  it should "parse File output config correctly (1)" in {
    val writer =  config.outputs.getByPhase(IR).head.asInstanceOf[SerializingQuadWriter]
    writer.filepath.endsWith("ldif-core/target/test-classes/ldif/config/silk-output.nt") should equal (true)
    writer.syntax.name should equal ("N-Triples")
  }

  it should "parse File output config correctly (2)" in {
    val writer =  config.outputs.getByPhase(COMPLETE).head.asInstanceOf[SerializingQuadWriter]
    writer.filepath.endsWith("ldif-core/target/test-classes/ldif/config/output.nq") should equal (true)
    writer.syntax.name should equal ("N-Quads")
  }
}