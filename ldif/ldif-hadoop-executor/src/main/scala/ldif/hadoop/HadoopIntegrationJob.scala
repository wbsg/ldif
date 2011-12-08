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

package ldif.hadoop

import config.HadoopIntegrationConfig
import entitybuilder.EntityBuilderHadoopExecutor
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import ldif.modules.r2r.R2RConfig
import de.fuberlin.wiwiss.r2r._
import ldif.entity.EntityDescription
import ldif.util.{StopWatch, LogUtil}
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.modules.r2r.hadoop.{R2RHadoopModule, R2RHadoopExecutor}
import ldif.modules.silk.SilkModule
import ldif.modules.silk.local.SilkLocalExecutor
import ldif.local.runtime.StaticEntityFormat
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.Configuration
import runtime.{RunHadoopUriMinting, RunHadoopUriTranslation, ConfigParameters, RunHadoopQuadToTextConverter}

class HadoopIntegrationJob(val config : HadoopIntegrationConfig, debug : Boolean = false) {

  private val log = LoggerFactory.getLogger(getClass.getName)
  val stopWatch = new StopWatch

  val sameAsPath = "sameAsFromSources"

  // Object to store all kinds of configuration data
  var configParameters = ConfigParameters(config.properties, sameAsPath)

  def runIntegration() {

    cleanup()

    stopWatch.getTimeSpanInSeconds()

    // Execute mapping phase
    val r2rOutput = mapQuads()
    log.info("Time needed to map data: " + stopWatch.getTimeSpanInSeconds + "s")

    // Execute linking phase
    val silkOutput = generateLinks(r2rOutput)
    log.info("Time needed to link data: " + stopWatch.getTimeSpanInSeconds + "s")

    var integratedPath = "integrated"

    // Execute URI Translation
    if(config.properties.getProperty("rewriteURIs", "true").toLowerCase=="true" && sameAsLinksAvailable()) {
//      translateUris(silkOutput, integratedPath)
      translateUris(r2rOutput, integratedPath)
      log.info("Time needed to translate URIs: " + stopWatch.getTimeSpanInSeconds + "s")
    } else
      integratedPath = r2rOutput
    RunHadoopQuadToTextConverter.execute(integratedPath, integratedPath+"_NQuads")
    RunHadoopUriMinting.execute(integratedPath, integratedPath+"_minted")
  }

  private def sameAsLinksAvailable(): Boolean = {
    val conf = new Configuration
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(sameAsPath)
    hdfs.exists(hdPath)
  }


  /**
   * Translates URIs
   */
  private def translateUris(inputPath : String, outputPath : String) {
    // TODO merge sameAs from sources and silk output
    RunHadoopUriTranslation.execute(inputPath, sameAsPath, outputPath)
  }

  /**
   * Transforms the Quads
   */
  private def mapQuads() : String = {
    val r2rExecutor = new R2RHadoopExecutor
    val r2rConfig = new R2RConfig(getMappingsRepository)
    val r2rModule = new R2RHadoopModule(r2rConfig)

    // R2RHadoop module has only one R2RHadoop Task, which contains all the mappings
    val r2rTask = r2rModule.tasks.head
    val entityDescriptions = (for(mapping <- r2rTask.ldifMappings) yield mapping.entityDescription).toSeq

    val entitiesPath =  "ebOutput-r2r"
    buildEntities(config.sources, entitiesPath, entityDescriptions, configParameters, getsTextInput = true)
    log.info("Time needed to load dump and build entities for mapping phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val r2rOutput = "r2rOutput"
    r2rExecutor.execute(r2rTask, Seq(new Path(entitiesPath)), new Path(r2rOutput))

    r2rOutput
  }

  /**
   * Build a mapping repository from a set of R2R mappings
   */
  private def getMappingsRepository : Repository = {
    val mappingSource = new FileOrURISource(new File(config.mappingDir))
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    new Repository(new JenaModelSource(importedMappingModel))
  }

  /**
   * Generates links.
   */
  private def generateLinks(quadsPath : String) : String =  {
    val silkModule = SilkModule.load(new File(config.linkSpecDir))
    val silkExecutor = new SilkLocalExecutor

    val entityDescriptions = silkModule.tasks.toIndexedSeq.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }
    val entitiesPath =  "ebOutput-silk"
    buildEntities(quadsPath, entitiesPath, entityDescriptions, configParameters)
    log.info("Time needed to build entities for linking phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val silkOutput = "silkOutput"

    // TODO - silk hadoop
    //      for((silkTask, readers) <- silkModule.tasks.toList zip entityReaders.grouped(2).toList)
    //      {
    //        silkExecutor.execute(silkTask, readers, outputQueue)
    //      }

    silkOutput
  }


  /**
   * Build Entities
   */
  private def buildEntities(sourcesPath : String, entitiesPath : String, entityDescriptions : Seq[EntityDescription], configParameters: ConfigParameters, getsTextInput: Boolean = false) {

    val entityBuilderConfig = new EntityBuilderConfig(entityDescriptions.toIndexedSeq)
    val entityBuilderModule = new EntityBuilderModule(entityBuilderConfig)
    val entityBuilderTask = entityBuilderModule.tasks.head
    val entityBuilderExecutor = new EntityBuilderHadoopExecutor(configParameters, getsTextInput)

    entityBuilderExecutor.execute(entityBuilderTask, List(new Path(sourcesPath)), List(new Path(entitiesPath)))
  }


  private def cleanup() {
    val hdfs = FileSystem.get(new Configuration())
    val hdPath = new Path(sameAsPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)
  }

}


object HadoopIntegrationJob {
  LogUtil.init
  private val log = LoggerFactory.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    if(args.length == 0) {
      log.warn("Usage: HadoopIntegrationJob <integration job config file>")
      System.exit(-1)
    }
    var debug = false
    val configFile = new File(args(args.length-1))

    if(args.length>=2 && args(0)=="--debug")
      debug = true

    val integrator = new HadoopIntegrationJob(HadoopIntegrationConfig.load(configFile))
    integrator.runIntegration()
  }
}