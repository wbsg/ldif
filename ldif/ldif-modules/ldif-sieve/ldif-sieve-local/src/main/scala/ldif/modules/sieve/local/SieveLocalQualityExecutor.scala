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
import ldif.modules.sieve.fusion.{FusionTask, FusionConfig, FusionFunction}
import ldif.modules.sieve.quality.QualityTask
import ldif.entity.{Node, Entity}
import ldif.runtime.QuadWriter

/**
 * Executes Sieve Data Fusion on a local machine.
 * @author pablomendes - based on Silk and R2R executors.
 */
class SieveLocalQualityExecutor(useFileInstanceCache: Boolean = false) extends Executor
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  //private val numThreads = 8
  //private val numThreads = Runtime.getRuntime.availableProcessors

  type TaskType = QualityTask

  type InputFormat = StaticEntityFormat

  type OutputFormat = GraphFormat

  def input(task : QualityTask) : InputFormat =
  {
    implicit val prefixes = task.qualityConfig.qualityConfig.prefixes

    // here we create entity descriptions from the quality metadata
    //val entityDescriptions = QualityConfig.createDummyEntityDescriptions(prefixes)
    val entityDescriptions = task.qualityConfig.qualityConfig.entityDescriptions

    new StaticEntityFormat(entityDescriptions)
  }

  def output(task : QualityTask) = new GraphFormat()

  /**
   * Executes a Sieve Quality task.
   */
  override def execute(task : QualityTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("Executing Sieve Quality Assessment Task %s".format(task.name))

    var numberOfNullEntities = 0

    task.qualitySpec.scoringFunctions

    // for each entity reader (one per input file?)
    reader.foreach( in => {
      val lastPatternId = in.entityDescription.patterns.size

      var entity : Entity = NoEntitiesLeft;
      while ( { entity = in.read(); entity != NoEntitiesLeft} ) {
        //log.info("Sieve Entity: %s".format(entity.resource.toString))
        //log.info("Patterns: "+in.entityDescription.patterns.size)

        // for scoringFunctions that need the graphId, use entity.resource, therefore the assumption below will not hold //TODO solve!
        assume(task.qualitySpec.scoringFunctions.size==in.entityDescription.patterns.size, "Number of scoringFunctions must be the same as number of patterns.")
        assume(task.qualitySpec.outputPropertyNames.size==in.entityDescription.patterns.size, "Number of outputPropertyNames must be the same as number of patterns.")

        if (entity==null) {
          numberOfNullEntities = numberOfNullEntities + 1
        }

        if (entity!=null && entity!=NoEntitiesLeft) {
          for (patternId <- 0 until lastPatternId) {
            val factums = entity.factums(patternId)
            val outputPropertyName = task.qualitySpec.outputPropertyNames(patternId)
            val scoringFunction = task.qualitySpec.scoringFunctions(patternId)
            log.debug("Pattern %s: ScoringFunction used: %s".format(patternId, scoringFunction))

            // score a graph according to each scoringFunction and write quads out
            val score = scoringFunction.score(factums)
            val scoreNode = Node.createTypedLiteral(score.toString,"http://www.w3.org/2001/XMLSchema#double")
            val quad = new Quad(entity.resource, outputPropertyName, scoreNode, task.qualityConfig.qualityMetadataGraph);
            writer.write(quad)
          }

        }
      }

      if (numberOfNullEntities>0)
        log.error("Found %d null entities. Is it normal?".format(numberOfNullEntities))

    })
  }

}