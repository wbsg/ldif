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

package ldif.local.datasources.sparql

import ldif.module.Executor
import ldif.local.runtime._
import ldif.datasources.sparql.SparqlTask

/**
 * Executor for the sparql data access module.
 */
class SparqlExecutor extends Executor
{
  type TaskType = SparqlTask
  type InputFormat = NoDataFormat
  type OutputFormat = DynamicEntityFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : SparqlTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : SparqlTask) : DynamicEntityFormat = DynamicEntityFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : SparqlTask, reader : Null, writers : Seq[EntityWriter])
  {
    val seb = new SparqlEntityBuilder(task.endpointUrl)

    for ((ed, i) <- task.entityDescriptions.zipWithIndex )
      seb.buildEntities(ed, writers(i))
  }
}