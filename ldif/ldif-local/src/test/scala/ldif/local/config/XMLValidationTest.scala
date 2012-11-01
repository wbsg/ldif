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

package ldif.local.config

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.io.File
import ldif.util.ValidationException
import org.scalatest.matchers.ShouldMatchers
import org.xml.sax.SAXParseException
import ldif.local.scheduler.ImportJob
import ldif.config.SchedulerConfig

@RunWith(classOf[JUnitRunner])
class XMLValidationTest extends FlatSpec with ShouldMatchers {

  it should "validate a valid configuration" in {
    val configFile = loadConfig("config/valid/scheduler-config.xml")
    SchedulerConfig.load(configFile)
  }

  it should "not validate an invalid configuration" in {
    val configFile = loadConfig("config/invalid/scheduler-config.xml")
    try {
      SchedulerConfig.load(configFile)
      fail()
    }
    catch {
      case e:ValidationException => {
        e.getMessage should equal ("The content of element 'scheduler' is not complete. One of '{\"http//ldif.wbsg.de/\"properties, \"http//ldif.wbsg.de/\"dumpLocation}' is expected.")
      }
    }
  }

  it should "not validate an invalid configuration (1)" in {
    val configFile = loadConfig("config/invalid/scheduler-config-1.xml")
    try {
      SchedulerConfig.load(configFile)
      fail()
    }
    catch {
      case e:SAXParseException => {
        e.getMessage should equal ("The element type \"integrationJob\" must be terminated by the matching end-tag \"</integrationJob>\".")
      }
    }
  }

  it should "not validate an invalid configuration (importJob)" in {
    val configFile = loadConfig("config/invalid/aba_import.xml")
    try {
      ImportJob.load(configFile)
      fail()
    }
    catch {
      case e:ValidationException => {
        e.getMessage should equal ("The value 'eekly' of element 'refreshSchedule' is not valid. Value 'eekly' is not facet-valid with respect to enumeration '[onStartup, always, hourly, daily, weekly, monthly, yearly, never]'. It must be a value from the enumeration.")
      }
    }
  }

  protected def loadConfig(config : String) =  {
    val configUrl = getClass.getClassLoader.getResource(config)
    new File(configUrl.toString.stripPrefix("file:"))
  }
}