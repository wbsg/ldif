/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.modules.silk.local

import ldif.module.Executor
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import de.fuberlin.wiwiss.silk.util.SourceTargetPair
import de.fuberlin.wiwiss.silk.instance.{Instance, MemoryInstanceCache, InstanceSpecification, FileInstanceCache}
import de.fuberlin.wiwiss.silk.datasource.Source
import de.fuberlin.wiwiss.silk.{OutputTask, FilterTask, MatchTask, LoadTask}
import de.fuberlin.wiwiss.silk.output.Output
import ldif.local.runtime._
import java.io.File
import org.apache.commons.io.FileUtils
import ldif.local.util.TemporaryFileCreator

/**
 * Executes Silk on a local machine.
 */
class SilkLocalExecutor(useFileInstanceCache: Boolean = false) extends Executor
{
  private val numThreads = 8
//  private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = SilkTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : SilkTask) =
  {
    implicit val prefixes = task.silkConfig.silkConfig.prefixes
    val entityDescriptions = CreateEntityDescriptions(task.linkSpec)

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : SilkTask) = new GraphFormat()

  /**
   * Executes a Silk task.
   */


  override def execute(task : SilkTask, reader : Seq[EntityReader], writer : QuadWriter)
  {
    val blocking = task.silkConfig.silkConfig.blocking
    val linkSpec = task.linkSpec

    //Retrieve Instance Specifications from Link Specification
    val instanceSpecs = InstanceSpecification.retrieve(linkSpec)

    //Create instance caches
    val caches = if(useFileInstanceCache) {
      val tempSource = TemporaryFileCreator.createTemporaryDirectory("ldif_silk_s", "", true)
      val tempTarget = TemporaryFileCreator.createTemporaryDirectory("ldif_silk_t", "", true)
      SourceTargetPair(
        new FileInstanceCache(instanceSpecs.source, tempSource, true, blocking.map(_.blocks).getOrElse(1)),
        new FileInstanceCache(instanceSpecs.target, tempTarget, true, blocking.map(_.blocks).getOrElse(1))
      )
    } else
      SourceTargetPair(
        new MemoryInstanceCache(instanceSpecs.source, blocking.map(_.blocks).getOrElse(1)),
        new MemoryInstanceCache(instanceSpecs.target, blocking.map(_.blocks).getOrElse(1))
      )


    //Load instances into cache
    val sources = SourceTargetPair.fromSeq(reader).map(reader => Source("id", LdifDataSource(reader)))
    def indexFunction(instance: Instance) = linkSpec.condition.index(instance, 0.0)

    val loadTask = new LoadTask(sources, caches, instanceSpecs, if (blocking.isDefined) Some(indexFunction _) else None)
    loadTask()

    //Execute matching
    val matchTask = new MatchTask(linkSpec, caches, numThreads, true)
    val links = matchTask()

    //Filter links
    val filterTask = new FilterTask(links, linkSpec.filter)
    val filteredLinks = filterTask()

    //Write links
    val linkWriter = new LdifLinkWriter(writer)
    val outputTask = new OutputTask(filteredLinks, linkSpec.linkType, Output("output", linkWriter) :: Nil)
    outputTask()
  }
}