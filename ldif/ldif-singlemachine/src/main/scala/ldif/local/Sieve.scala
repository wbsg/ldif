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

import rest.MonitorServer
import impl._
import java.io._
import org.slf4j.LoggerFactory
import ldif.util._
import ldif.config._
import scala.collection.mutable.{Set => MSet}

case class Sieve (config : IntegrationConfig, debugMode : Boolean = false) {

  private val log = LoggerFactory.getLogger(getClass.getName)

    def main(args: Array[String]) {
        val parser = new scopt.mutable.OptionParser("Sieve", "0.2") {
          opt("o", "output", "<file>", "output is a string property", { v: String => config.bar = v })
          arg("<singlefile>", "<singlefile> is an argument", { v: String => config.whatnot = v })
          // arglist("<file>...", "arglist allows variable number of arguments",
          //   { v: String => config.files = (v :: config.files).reverse })
        }
        if (parser.parse(args)) {
          // do stuff
        }
        else {
          // arguments are bad, usage message will have been displayed
        }

        var debug = false
        val configFile = new File(args(args.length - 1))

        if (args.length >= 2 && args(0) == "--debug")
            debug = true

        val integrator = IntegrationJob.load(configFile, debug)

        val runStatusMonitor = integrator.config.properties.getProperty("runStatusMonitor", "true").toLowerCase == "true"
        val statusMonitorURI = integrator.config.properties.getProperty("statusMonitorURI", Consts.DefaultStatusMonitorrURI)

        // Start REST HTTP Server
        if (runStatusMonitor)
            MonitorServer.start(statusMonitorURI)

        integrator.runIntegration
        sys.exit(0)
    }
}





