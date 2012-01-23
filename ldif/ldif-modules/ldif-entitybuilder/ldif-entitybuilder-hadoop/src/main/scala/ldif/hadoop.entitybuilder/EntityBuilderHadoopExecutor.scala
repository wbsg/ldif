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

package ldif.hadoop.entitybuilder

import ldif.module.Executor
import java.util.Properties
import ldif.util.FatalErrorListener
import ldif.EntityBuilderTask
import ldif.hadoop.runtime._
import org.apache.hadoop.fs.Path

class EntityBuilderHadoopExecutor(configParameters: ConfigParameters = ConfigParameters(new Properties)) extends Executor {

  type TaskType = EntityBuilderTask
  type InputFormat = QuadFormat
  type OutputFormat = DynamicEntityFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : EntityBuilderTask) : QuadFormat = QuadFormat()

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
  override def execute(task : EntityBuilderTask, reader : Seq[Path], writer : Seq[Path])  {
    val eb = new HadoopEntityBuilder(task.entityDescriptions, reader, configParameters)
    eb.buildEntities(writer.head)
  }

}
