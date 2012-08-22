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

import ldif.config.SchedulerConfig
import java.io.File
import org.slf4j.LoggerFactory
import rest.MonitorServer
import ldif.util.{CommonUtils, Consts, ValidationException, LogUtil}

object Ldif {
  LogUtil.init
  private val log = LoggerFactory.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    var debug = false
    if(args.length==0) {
      log.warn("No configuration file given.")
      printHelpAndExit()
    }
    else if(args.length>=2 && args(0)=="--debug")
      debug = true

    val configFile = if(args.length == 0) {
      val configUrl = getClass.getClassLoader.getResource("ldif/local/neurowiki/scheduler-config.xml")
      new File(configUrl.toString.stripPrefix("file:"))
    } else
        CommonUtils.getFileFromPathOrUrl(args(args.length-1))

    if(!configFile.exists){
      log.warn("Configuration file not found at "+ configFile.getCanonicalPath)
      printHelpAndExit()
    }
    else {
      // Setup Scheduler
      var config : SchedulerConfig = null
      try {
        config = SchedulerConfig.load(configFile)
      }
      catch {
        case e:ValidationException => {
          log.error("Invalid Scheduler configuration: "+e.toString +
            "\n- More details: " + Consts.xsdScheduler)
          System.exit(1)
        }
      }
      val scheduler = Scheduler(config, debug)

      val runStatusMonitor = config.properties.getProperty("runStatusMonitor", "true").toLowerCase=="true"
      val statusMonitorURI = config.properties.getProperty("statusMonitorURI", Consts.DefaultStatusMonitorrURI)

      // Start REST HTTP Server
      if(runStatusMonitor)
        MonitorServer.start(statusMonitorURI)

      // check if dumpDir exists or can be created
      val dumpDir = new File(config.dumpLocationDir)
      if (!dumpDir.exists && !dumpDir.mkdir)  {
        log.error("Dump location doesn't exists and can't be created")
        sys.exit(1)
      }

      scheduler.run(true)
    }
  }

  def printHelpAndExit() {
    log.info(Consts.LDIF_HELP_HEADER+
      "\nUsages: ldif <schedulerConfig.xml>" + //schedulerConfiguration refers to scheduler.properties or schedulerConfig.xml?
      "\n\tldif-integrate <integrationJob.xml>" + //integrationJobConfiguration refers to integration.properties or integrationJob.xml?
      Consts.LDIF_HELP_FOOTER)
    System.exit(1)
  }


}