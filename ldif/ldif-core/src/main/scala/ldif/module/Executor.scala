/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.module

/**
 * An executor executes a module task.
 */
trait Executor
{
  /**
   * The type of the tasks supported by this executor.
   */
  type TaskType <: ModuleTask

  /**
   * The type of the input format accepted by this executor.
   */
  type InputFormat <: DataFormat

  /**
   * The type of the output format accepted by this executor.
   */
  type OutputFormat <: DataFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  def input(task : TaskType) : InputFormat

  /**
   * Determines the output format of a specific task.
   */
  def output(task : TaskType) : OutputFormat

  /**
   * Executes a specific task.
   *
   * @param task The rask to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  def execute(task : TaskType, reader : InputFormat#Reader, writer : OutputFormat#Writer)
}