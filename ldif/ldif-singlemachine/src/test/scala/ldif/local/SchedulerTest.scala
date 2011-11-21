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

import config.SchedulerConfig
import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scheduler._

@RunWith(classOf[JUnitRunner])
class SchedulerTest extends FlatSpec with ShouldMatchers {

  val configFile = loadConfig("ldif/local/resources/scheduler/scheduler-config.xml")
  val scheduler = new Scheduler(SchedulerConfig.load(configFile))

      /* Disabled - remote test */

//  it should "schedule a job correctly" in {
//    scheduler.checkUpdate(scheduler.importJobs.head) should equal (true)
//  }
//
//  it should "parse a job configuration correctly" in {
//    scheduler.importJobs.head should equal (job)
//  }
//
//  it should "load a dump correctly" in {
//    scheduler.runUpdate
//    //TODO check dump is correct
//    true should equal (true)
//  }

  protected def loadConfig(config : String) =  {
    val configUrl = getClass.getClassLoader.getResource(config)
    new File(configUrl.toString.stripPrefix("file:"))
  }

  lazy val job = {
    val url = "http://www.assembla.com/code/ldif/git/node/blob/ldif/ldif-singlemachine/src/test/resources/ldif/local/resources/sources/aba.nq.bz2"
    new QuadImportJob(url,"ABA.0","always","ABA")
  }
}

