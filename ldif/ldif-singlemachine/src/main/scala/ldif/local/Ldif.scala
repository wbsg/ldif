/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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
import org.slf4j.LoggerFactory
import ldif.util.LogUtil
;


object Ldif {
  LogUtil.init
  private val log = LoggerFactory.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    var debug = false
    if(args.length<1) {
      log.warn("No configuration file given.")
      System.exit(1)
    }
    else if(args.length>=2 && args(0)=="--debug")
      debug = true

    val configFile = if(args.length == 0) {
      val configUrl = getClass.getClassLoader.getResource("ldif/local/neurowiki/scheduler-config.xml")
      new File(configUrl.toString.stripPrefix("file:"))
    } else
      new File(args(args.length-1))

    if(!configFile.exists)
      log.warn("Configuration file not found at "+ configFile.getCanonicalPath)
    else {
      // Setup Scheduler
      val config = SchedulerConfig.load(configFile)
      val scheduler = new Scheduler(config, debug)

      // Evaluate jobs at most once. Evaluate import first, then integrate.
      if (scheduler.runOnce) {
        scheduler.evaluateImportJobs
        Thread.sleep(1000)
        while (!scheduler.allJobsCompleted) {
            // wait for jobs to be completed
            Thread.sleep(1000)
        }
        scheduler.evaluateIntegrationJob(false)
        sys.exit(0)
      }
      else {
        log.info("Running LDIF as server")
        // Evaluate jobs every 10 sec, run as server
        while(true){
          scheduler.evaluateJobs
          Thread.sleep(10 * 1000)
        }
      }
    }
  }

}