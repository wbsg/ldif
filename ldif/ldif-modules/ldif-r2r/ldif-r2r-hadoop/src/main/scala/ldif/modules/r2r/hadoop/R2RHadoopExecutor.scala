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

package ldif.modules.r2r.hadoop

import org.slf4j.LoggerFactory
import ldif.modules.r2r.R2RTask
import ldif.hadoop.runtime._
import org.apache.hadoop.fs.Path
import ldif.module.Executor

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/16/11
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopExecutor extends Executor {
  private val log = LoggerFactory.getLogger(getClass.getName)
  type TaskType = R2RHadoopTask
  type InputFormat = EntityFormat
  type OutputFormat = QuadFormat

  def input(task: R2RHadoopTask) = EntityFormat(for(mapping <- task.ldifMappings) yield mapping.entityDescription)

  def output(task: R2RHadoopTask) = new QuadFormat()

  override def execute(task: R2RHadoopTask, reader: Seq[Path], writer: Path) {
    val mappings = task.ldifMappings
    val inputPath = reader.head.toString
    val outputPath = writer.toString
    RunHadoopR2RJob.execute(inputPath, outputPath, mappings)
  }
}