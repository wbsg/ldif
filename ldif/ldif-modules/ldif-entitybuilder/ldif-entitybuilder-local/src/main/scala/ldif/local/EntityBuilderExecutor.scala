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

import ldif.module.Executor
import ldif.local.runtime._
import java.util.Properties
import ldif.util.{MemoryUsage, FatalErrorListener}
import ldif.EntityBuilderTask

class EntityBuilderExecutor(configParameters: ConfigParameters = ConfigParameters(new Properties, null)) extends Executor {

  type TaskType = EntityBuilderTask
  type InputFormat = GraphFormat
  type OutputFormat = DynamicEntityFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : EntityBuilderTask) : GraphFormat = GraphFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : EntityBuilderTask) : DynamicEntityFormat = DynamicEntityFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : EntityBuilderTask, reader : Seq[QuadReader], writer : Seq[EntityWriter])
  {
    val eb = EntityBuilderFactory.getEntityBuilder(configParameters, task.entityDescriptions, reader)
    val inmemory = configParameters.configProperties.getProperty("entityBuilderType", "in-memory")=="in-memory"

//    println("Memory used (before build entities): " + MemoryUsage.getMemoryUsage())   //TODO: remove
    for ((ed, i) <- task.entityDescriptions.zipWithIndex ) {
      if(inmemory)
        runInBackground {
          eb.buildEntities(ed, writer(i))
        }
      else
        eb.buildEntities(ed, writer(i))
    }
  }

  /**
   * Evaluates an expression in the background.
   */
  private def runInBackground(function : => Unit) {
    val thread = new Thread {
      private val listener: FatalErrorListener = FatalErrorListener

      override def run {
        try {
          function
        } catch {
          case e: Exception => listener.reportError(e)
        }
      }
    }
    thread.start
  }
}
