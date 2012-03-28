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

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/10/12
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */

object MainClassDispatcher {
  def main(args : Array[String])
  {
    if(args.length<1) {
      System.err.println("Error: No arguments given.")
      exitDispatcher
    }
    val command = args(0)
    val parameters = args.slice(1, args.length) // remove command from array
    command match {
      case "scheduler" => Ldif.main(parameters)
      case "integrate" => HadoopIntegrationJob.main(parameters)
      case "urisets" => UriSets.execute(parameters)
      case "r2r" => R2R.execute(parameters)
      case "silk" => Silk.execute(parameters)
      case _ => System.err.println("Error: command " + command + " invalid.")
        exitDispatcher
    }
  }

  private def exitDispatcher {
    System.err.println("Usages: hadoop jar ldif-hadoop-executor* scheduler <schedulerConfig>")
    System.err.println("        hadoop jar ldif-hadoop-executor* integrate <integrationJobConfig>")
    System.err.println("        hadoop jar ldif-hadoop-executor* urisets <input path> <output path>")
    System.err.println("        hadoop jar ldif-hadoop-executor* r2r <local path to mappings> <input path> <output path>")
    System.err.println("        hadoop jar ldif-hadoop-executor* silk <local path to link spec> <input path> <output path>")
    System.exit(1)
  }
}