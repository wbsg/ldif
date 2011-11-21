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

package ldif.modules.r2r.local

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 13.05.11
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */

import ldif.module.Executor
import de.fuberlin.wiwiss.r2r._
import ldif.modules.r2r._
import ldif.local.runtime.{GraphFormat, StaticEntityFormat, QuadWriter, EntityReader}
import collection.mutable.ArrayBuffer
import ldif.entity.Entity
import java.util.logging.Logger

class R2RLocalExecutor extends Executor {
  val maxEntitesPerIterable = 1000
  private val log = Logger.getLogger(getClass.getName)
  type TaskType = R2RTask
  type InputFormat = StaticEntityFormat
  type OutputFormat = GraphFormat

  def input(task: R2RTask) = StaticEntityFormat(Seq(task.mapping.entityDescription))

  def output(task: R2RTask) = new GraphFormat()

  override def execute(task: R2RTask, reader: Seq[EntityReader], writer: QuadWriter) {
    val mapping = task.mapping
    val inputQueue = reader.head
    log.info("Executing mapping <" + task.mapping.mapping.getUri + ">")

    while(inputQueue.hasNext) {
      var counter = 0
      val entities = new ArrayBuffer[Entity]

      while(inputQueue.hasNext && counter < maxEntitesPerIterable) {
        entities.append(inputQueue.read())
        counter += 1
      }
      for(quad <- mapping.executeMappingMT(entities).toList) {
        writer.write(quad)
      }
    }
  }
}

//class R2RLocalExecutor extends Executor {
//  type TaskType = R2RTask
//  type InputFormat = StaticEntityFormat
//  type OutputFormat = GraphFormat
//
//  def input(task: R2RTask) = StaticEntityFormat(Seq(task.mapping.entityDescription))
//
//  def output(task: R2RTask) = new GraphFormat()
//
//  override def execute(task: R2RTask, reader: Seq[EntityReader], writer: QuadWriter) {
//    val entityDescription = task.mapping.entityDescription
//    val mapping = task.mapping
//    val inputQueue = reader.head
//
//    while(inputQueue.hasNext) {
//      val entity = inputQueue.read
//      mapping.executeMapping(entity, writer)
//    }
//  }
//}