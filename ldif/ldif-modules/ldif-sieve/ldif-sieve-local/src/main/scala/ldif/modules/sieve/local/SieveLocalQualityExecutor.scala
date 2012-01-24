package ldif.modules.sieve.local

/*
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

import ldif.module.Executor
import ldif.local.runtime._
import impl.NoEntitiesLeft
import org.slf4j.LoggerFactory
import ldif.runtime.Quad
import ldif.entity.Entity
import ldif.modules.sieve.fusion.{FusionTask, FusionConfig, FusionFunction}

/**
 * Executes Sieve Data Fusion on a local machine.
 * @author pablomendes - based on Silk and R2R executors.
 */
class SieveLocalQualityExecutor(useFileInstanceCache: Boolean = false) extends Executor
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  //private val numThreads = 8
  //private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = FusionTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : FusionTask) : InputFormat =
  {
    implicit val prefixes = task.sieveConfig.sieveConfig.prefixes

    // here we create entity descriptions from the quality metadata
    //val entityDescriptions = QualityConfig.createDummyEntityDescriptions(prefixes)
    val entityDescriptions = task.sieveConfig.sieveConfig.entityDescriptions

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : FusionTask) = new GraphFormat()

  /**
   * Executes a Sieve Quality task.
   */
  override def execute(task : FusionTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("Executing Sieve Task %s".format(task.name))

  }

}
