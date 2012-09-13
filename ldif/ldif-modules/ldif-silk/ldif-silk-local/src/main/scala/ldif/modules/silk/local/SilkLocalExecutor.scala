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

package ldif.modules.silk.local

import ldif.module.Executor
import ldif.modules.silk.{CreateEntityDescriptions, SilkTask}
import de.fuberlin.wiwiss.silk.datasource.Source
import de.fuberlin.wiwiss.silk.{OutputTask, FilterTask, MatchTask, LoadTask}
import de.fuberlin.wiwiss.silk.output.Output
import ldif.local.runtime._
import de.fuberlin.wiwiss.silk.entity.{Entity => SilkEntity}
import de.fuberlin.wiwiss.silk.entity.{EntityDescription => SilkEntityDescription}
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.cache.{FileEntityCache, MemoryEntityCache}
import de.fuberlin.wiwiss.silk.config.RuntimeConfig
import ldif.runtime.QuadWriter
import ldif.util.TemporaryFileCreator
import java.io.File

/**
 * Executes Silk on a local machine.
 */
class SilkLocalExecutor(useFileInstanceCache: Boolean = false, allowLinksForSameURIs: Boolean = false) extends Executor {

  type TaskType = SilkTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  val reporter = new SilkReportPublisher

  def input(task : SilkTask) = {
    implicit val prefixes = task.silkConfig.silkConfig.prefixes
    val entityDescriptions = CreateEntityDescriptions(task.linkSpec)

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : SilkTask) = new GraphFormat()

  /**
   * Executes a Silk task.
   */
  override def execute(task : SilkTask, reader : Seq[EntityReader], writer : QuadWriter) {
    val config = RuntimeConfig()
    val linkSpec = task.linkSpec
    val entityDescs = linkSpec.entityDescriptions
    var tempFiles = Seq[File]()

    reporter.setStartTime()
    reporter.setStatus("10%") // Building instance caches (1/5)
    //Create instance caches
    val caches = if(useFileInstanceCache) {
      val tempSource = TemporaryFileCreator.createTemporaryDirectory("ldif_silk_s", "", true)
      val tempTarget = TemporaryFileCreator.createTemporaryDirectory("ldif_silk_t", "", true)
      tempFiles = Seq(tempSource, tempTarget)
      DPair(
        new FileEntityCache(entityDescs.source, linkSpec.rule.index(_), tempSource, config),
        new FileEntityCache(entityDescs.target, linkSpec.rule.index(_), tempTarget, config)
      )
    } else
      DPair(
        new MemoryEntityCache(entityDescs.source, linkSpec.rule.index(_)),
        new MemoryEntityCache(entityDescs.target, linkSpec.rule.index(_))
      )

    reporter.setStatus("30%") // Loading instance into caches (2/5)
    //Load instances into cache
    val sources = DPair.fromSeq(reader).map(reader => Source("id", LdifDataSource(reader)))

    val loadTask = new LoadTask(sources, caches)
    loadTask()

    reporter.setStatus("50%")  // Executing matching (3/5)
    //Execute matching
    val matchTask = new MatchTask(linkSpec.rule, caches, config)
    val links = matchTask()

    reporter.setStatus("70%") //Filtering links (4/5)
    //Filter links
    val filterTask = new FilterTask(links, linkSpec.filter)
    val filteredLinks = filterTask()

    reporter.setStatus("90%") // Writing links (5/5)")
    //Write links
    val linkWriter = new LdifLinkWriter(writer, allowLinksForSameURIs)
    val outputTask = new OutputTask(filteredLinks, linkSpec.linkType, Output("output", linkWriter) :: Nil)
    outputTask()
    for(tempFile <- tempFiles)
      TemporaryFileCreator.deleteDirOnExit(tempFile)
  }
}