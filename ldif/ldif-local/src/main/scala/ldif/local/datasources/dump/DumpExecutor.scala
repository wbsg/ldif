/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

package ldif.local.datasources.dump

import ldif.datasources.dump.DumpTask
import ldif.module.Executor
import ldif.local.runtime._
import java.io.{BufferedReader, InputStreamReader}
import ldif.runtime.QuadWriter

/**
 * Executor for the dump data source.
 */
class DumpExecutor extends Executor
{
  type TaskType = DumpTask
  type InputFormat = NoDataFormat
  type OutputFormat = GraphFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : DumpTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : DumpTask) : GraphFormat = GraphFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : DumpTask, reader : Null, writer : QuadWriter)
  {
    val inputStream = DumpLoader.getStream(task.sourceLocation)
    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    val quadParser = new QuadFileLoader(task.name)
    quadParser.readQuads(bufferedReader, writer)
    
    writer.finish
  }
}