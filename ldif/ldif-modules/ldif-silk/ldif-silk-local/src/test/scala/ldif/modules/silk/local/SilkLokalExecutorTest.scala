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

package ldif.modules.silk.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.modules.silk.{SilkModuleConfig, SilkModule}
import de.fuberlin.wiwiss.silk.impl.DefaultImplementations
import ldif.util.Prefixes
import ldif.entity.EntityDescription
import xml.XML
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import de.fuberlin.wiwiss.silk.config.SilkConfig

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

    val config = SilkConfig.load(configStream)

    val module = new SilkModule(new SilkModuleConfig(config))

    module.tasks.head
  }

  private lazy val entityDescription =
  {
    implicit val prefixes = Prefixes(task.silkConfig.silkConfig.prefixes)

    val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/silk/local/PharmGKB_EntityDescription.xml")

    EntityDescription.fromXML(XML.load(stream))
  }
}