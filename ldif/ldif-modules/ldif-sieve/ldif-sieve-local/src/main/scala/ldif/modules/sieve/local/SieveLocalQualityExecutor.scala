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
   // val entityDescriptions = task.qualityConfig.qualityConfig.entityDescriptions

    //new StaticEntityFormat(entityDescriptions)
    null
    // TODO: fix this? do we need this method at all?
  }

  def output(task : QualityTask) = new GraphFormat()

  /**
   * Executes a Sieve Quality task.
   */
  override def execute(task : QualityTask, reader : Seq[EntityReader], writer : QuadWriter) {
    log.info("Executing Sieve Quality Assessment Task [%s]".format(task.name))

    var numberOfNullEntities = 0

    if (reader.size==0)
      log.info("Empty EntityReader provided to QualityExecutor. Will do nothing.")

    // for each entity reader (one per input file?)
    reader.foreach( in => {

      val lastPatternId = in.entityDescription.patterns.size

      var entity : Entity = NoEntitiesLeft;
      while ( { entity = in.read(); entity != NoEntitiesLeft} ) {

        // for scoringFunctions that need the graphId, use entity.resource, therefore the assumption below will not hold
      //  assume(task.qualitySpec.scoringFunctions.size==in.entityDescription.patterns.size, "Number of scoringFunctions must be the same as number of patterns.")
       // assume(task.qualitySpec.outputPropertyNames.size==in.entityDescription.patterns.size, "Number of outputPropertyNames must be the same as number of patterns.")

        if (entity==null) {
          numberOfNullEntities = numberOfNullEntities + 1
        }

        if (entity!=null && entity!=NoEntitiesLeft) {
          log.trace("Sieve Entity: %s".format(entity.resource))
          log.trace("nPatterns: "+in.entityDescription.patterns.size)

          for (patternId <- 0 until lastPatternId) {
            val facta = entity.factums(patternId)
            val outputPropertyName = task.qualitySpec.outputPropertyNames(patternId)
            val scoringFunction = task.qualitySpec.scoringFunctions(patternId)

            //log.trace("Pattern %s: ScoringFunction used: %s".format(patternId, scoringFunction))
            //log.trace("nFactums: %d".format(factums.size))

            // score a graph according to each scoringFunction and write quads out
            val score = scoringFunction.score(entity.resource, facta)
            val scoreNode = Node.createTypedLiteral(score.toString,"http://www.w3.org/2001/XMLSchema#double")
            val quad = new Quad(entity.resource, outputPropertyName, scoreNode, task.qualityConfig.qualityMetadataGraph);
            writer.write(quad)
          }

        }
      }

      if (numberOfNullEntities>0)
        log.error("Found %d null entities. Is this normal?".format(numberOfNullEntities))

    })
  }

}
