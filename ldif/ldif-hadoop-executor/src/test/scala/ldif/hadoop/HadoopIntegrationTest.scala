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

import config.HadoopIntegrationConfig
import java.io.File
import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.Node
import ldif.runtime.Quad
import org.scalatest.matchers.ShouldMatchers
import ldif.util.OutputValidator


@RunWith(classOf[JUnitRunner])
class HadoopIntegrationTest extends FlatSpec with ShouldMatchers {

  it should "run the hadoop integration flow correctly" in {
      // Run LDIF
      val configFile = loadConfig("ldif/hadoop/integrationJob.xml")
      val ldifOutput = runLdif(configFile, true)

      // Load results to compare with
      val ldimporterOuputUrl = getClass.getClassLoader.getResource("ldif/hadoop/result.nq")
      val ldimporterOuputFile = new File(ldimporterOuputUrl.toString.stripPrefix("file:"))

      // Check results
      OutputValidator.compare(ldifOutput, ldimporterOuputFile) should equal (0)

  }


    protected def loadConfig(config : String) =  {
      val configUrl = getClass.getClassLoader.getResource(config)
      new File(configUrl.toString.stripPrefix("file:"))
    }

    protected def runLdif(configFile : File, debugMode: Boolean = false) = {
      val integrator = new HadoopIntegrationJob(HadoopIntegrationConfig.load(configFile), debugMode)
      integrator.runIntegration
      (new File(integrator.config.outputFile)).listFiles().last
    }

}