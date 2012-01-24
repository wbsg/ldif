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

package ldif.hadoop

import config.HadoopIntegrationConfig
import entitybuilder.EntityBuilderHadoopExecutor
import io.EntityMultipleSequenceFileOutput
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import ldif.modules.r2r.R2RConfig
import de.fuberlin.wiwiss.r2r._
import ldif.entity.EntityDescription
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.modules.r2r.hadoop.{R2RHadoopModule, R2RHadoopExecutor}
import ldif.modules.silk.SilkModule
import org.apache.hadoop.conf.Configuration
import ldif.modules.silk.hadoop.SilkHadoopExecutor
import runtime._
import de.fuberlin.wiwiss.silk.util.DPair
import java.util.Calendar
import ldif.util.{ValidationException, Consts, StopWatch, LogUtil, RemoteSparqlEndpoint}
import org.apache.hadoop.fs.{FileSystem, Path, FSDataInputStream}
import ldif.datasources.dump.QuadParser
import java.net.URI

class HadoopIntegrationJob(val config : HadoopIntegrationConfig, debug : Boolean = false) {

  private val log = LoggerFactory.getLogger(getClass.getName)
  val stopWatch = new StopWatch

  var lastUpdate : Calendar = null

  val conf = new Configuration
  val hdfs = FileSystem.get(conf)

  val useExternalSameAsLinks = config.properties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
  val outputAllQuads = config.properties.getProperty("output", "mapped-only").toLowerCase=="all"
  val rewriteUris = config.properties.getProperty("rewriteURIs", "true").toLowerCase=="true"
  val uriMinting = config.properties.getProperty("uriMinting", "false").toLowerCase=="true"
  val ignoreProvenance = config.properties.getProperty("outputFormat", "nq").toLowerCase=="nt"

  val externalSameAsLinksDir = clean("sameAsFromSources")
  // Contains quads that are not processed but must be added to the output
  val allQuadsDir = clean("allQuads")
  // Contains provenance quads
  val provenanceQuadsDir = clean("provenanceQuads")

  def runIntegration() {

    val sourcesPath = new Path(config.sources)
    val sourceNumber = hdfs.listStatus(sourcesPath).length
    log.info("Hadoop Integration Job started")
    log.info("- Input < "+ sourceNumber +" sources found in " + sourcesPath.toString)
    log.info("- Output > "+ config.outputFile)
    log.info("- Properties ")
    for (key <- config.properties.keySet.toArray)
      log.info("  - "+key +" : " + config.properties.getProperty(key.toString) )

    stopWatch.getTimeSpanInSeconds()

    // Execute mapping phase
    val r2rOutput = mapQuads()
    log.info("Time needed to map data: " + stopWatch.getTimeSpanInSeconds + "s")

    // Execute linking phase
    val silkOutput = generateLinks(r2rOutput)
    log.info("Time needed to link data: " + stopWatch.getTimeSpanInSeconds + "s")

    // Prepare data to be translated
    move(allQuadsDir, r2rOutput)
    var sameAsLinks : String = null
    if(useExternalSameAsLinks)
      sameAsLinks = getAllLinks(silkOutput, new Path(externalSameAsLinksDir))
    else sameAsLinks = getAllLinks(silkOutput)

    var outputPath = r2rOutput

    // Execute URI Translation (if enabled)
    if(rewriteUris) {
      outputPath = translateUris(outputPath, sameAsLinks)
      log.info("Time needed to translate URIs: " + stopWatch.getTimeSpanInSeconds + "s")
    }

    // Execute URI Minting (if enabled)
    if(uriMinting) {
      // TODO collect minting-properties quads in the previous HEB-phase2 (both r2r and silk)
      //      and use (only) those quads as input for the URI minting job
      outputPath = mintUris(outputPath)
      log.info("Time needed to mint URIs: " + stopWatch.getTimeSpanInSeconds + "s")
    }

    // add provenance quads to the output path
    move(provenanceQuadsDir, outputPath)
    // add sameAs links to the output path
    move(sameAsLinks, outputPath)

    writeOutput(outputPath)

    lastUpdate = Calendar.getInstance
  }

  /**
   *  Mints URIs
   */
  private def mintUris(inputPath : String) : String = {
    val mintedUriPath = inputPath+"_minted"
    val (mintNamespace, mintPropertySet) = getMintValues(config)
    HadoopUriMinting.execute(inputPath, mintedUriPath, mintNamespace, mintPropertySet)
    clean(inputPath)
    mintedUriPath
  }

  private def getMintValues(config: HadoopIntegrationConfig): (String, Set[String]) = {
    val mintNamespace = config.properties.getProperty("uriMintNamespace")
    val mintPropertySet = config.properties.getProperty("uriMintLabelPredicate").split("\\s+").toSet
    (mintNamespace, mintPropertySet)
  }


  /**
   * Translates URIs
   */
  private def translateUris(inputPath : String, sameAsLinks : String) : String = {
    val outputPath = inputPath+"_translated"
    RunHadoopUriTranslation.execute(inputPath, sameAsLinks, outputPath)
    clean(inputPath)
    outputPath
  }


  /**
   * Merges sameAs links from sources and silk output
   */
  private def getAllLinks(sameAsFromSilk : Seq[Path], sameAsFromSource : Path = null) : String = {

    val allSameAsLinks = clean(new Path("allSameAsLinks"))
    hdfs.mkdirs(allSameAsLinks)

    for (path <- sameAsFromSilk) {
      val sameAsFromSilkSeq = hdfs.listStatus(path)
      for (status <- sameAsFromSilkSeq.filterNot(_.getPath.getName.startsWith("_")))
        hdfs.rename(status.getPath, new Path(allSameAsLinks+Consts.fileSeparator+path.getName+status.getPath.getName))
    }
    if(!debug) clean(sameAsFromSilk.head.getParent)

    if (sameAsFromSource != null) {
      move(sameAsFromSource, allSameAsLinks, false)
    }

    allSameAsLinks.toString
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

    val entitiesPath =  clean("ebOutput-r2r")
    var configParameters = ConfigParameters(config.properties, null, null, null, true)
    if (useExternalSameAsLinks)
      configParameters = configParameters.copy(sameAsPath = externalSameAsLinksDir)
    if (outputAllQuads)
      configParameters = configParameters.copy(allQuadsPath = allQuadsDir)
    if (!ignoreProvenance)
      configParameters = configParameters.copy(provenanceQuadsPath = provenanceQuadsDir)
    buildEntities(config.sources, entitiesPath, entityDescriptions, configParameters)
    log.info("Time needed to load dump and build entities for mapping phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val r2rOutput = "r2rOutput"
    r2rExecutor.execute(r2rTask, Seq(new Path(entitiesPath)), new Path(r2rOutput))

    clean(entitiesPath)
    r2rOutput
  }

  /**
   * Build a mapping repository from a set of R2R mappings
   */
  private def getMappingsRepository : Repository = {
    val mappingSource = new FileOrURISource(config.mappingDir)
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    new Repository(new JenaModelSource(importedMappingModel))
  }

  /**
   * Generates links.
   */
  private def generateLinks(quadsPath : String) : Seq[Path] =  {
    val entitiesDirectory =  clean("ebOutput-silk")
    val outputDirectory = clean("silkOutput")

    val silkModule = SilkModule.load(config.linkSpecDir)
    val silkExecutor = new SilkHadoopExecutor
    val tasks = silkModule.tasks.toIndexedSeq
    val entityDescriptions = tasks.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }

    var configParameters = ConfigParameters(config.properties)
    buildEntities(quadsPath, entitiesDirectory, entityDescriptions, configParameters)
    log.info("Time needed to build entities for linking phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val result = for((silkTask, i) <- tasks.zipWithIndex) yield {
      val sourcePath = new Path(entitiesDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i * 2))
      val targetPath = new Path(entitiesDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i * 2 + 1))
      val outputPath = new Path(outputDirectory, EntityMultipleSequenceFileOutput.generateDirectoryName(i))

      silkExecutor.execute(silkTask, DPair(sourcePath, targetPath), outputPath)

      outputPath
    }
    if(!debug) clean(entitiesDirectory)
    result
  }

  /**
   * Build Entities
   */
  private def buildEntities(sourcesPath : String, entitiesPath : String, entityDescriptions : Seq[EntityDescription], configParameters: ConfigParameters) {

    val entityBuilderConfig = new EntityBuilderConfig(entityDescriptions.toIndexedSeq)
    val entityBuilderModule = new EntityBuilderModule(entityBuilderConfig)
    val entityBuilderTask = entityBuilderModule.tasks.head
    val entityBuilderExecutor = new EntityBuilderHadoopExecutor(configParameters)

    entityBuilderExecutor.execute(entityBuilderTask, List(new Path(sourcesPath)), List(new Path(entitiesPath)))
  }


  private def clean(path : String) : String =  {
    clean(new Path(path))
    path
  }

  private def clean(hdPath : Path) : Path =  {
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)
    hdPath
  }

  private def writeOutput(outputPath : String)    {
    val nqOutput = config.properties.getProperty("outputFormat", "nq").toLowerCase.equals("nq")

    // convert output files from seq to nq
    HadoopQuadToTextConverter.execute(outputPath, config.outputFile)

    // SPARQL Output

    // basic setup
    var endpoint = new RemoteSparqlEndpoint(new URI("http://ec2-176-34-208-158.eu-west-1.compute.amazonaws.com:10035/repositories/ldif-test"), Some("ldif", "1d1f"));
    var instream = hdfs.open(new Path(config.outputFile))

    val lines = scala.io.Source.fromInputStream(instream).getLines
    val parser = new QuadParser

    // loop and stop as the first lastUpdate quad is found
    for (quad <- lines.toTraversable.map(parser.parseLine(_))){
      log.debug(quad.toString());
      // every 500 lines:
      // endpoint.executeQuery("INSERT INTO <" + graph + "> {\n" + quads + "\n}");
    }

    // TODO add output module

    clean(outputPath)
  }

  // Move files from one path to another
  private def move (from : String, dest : String, cleanDest : Boolean = false) {
    move(new Path(from), new Path(dest), cleanDest)
  }

  private def move (from : Path, dest : Path, cleanDest : Boolean) {
    log.debug("Moving files from "+from.toString+" to "+ dest.toString)
    // move files
    val filesFrom = hdfs.listStatus(from)

    if (filesFrom != null) {
      if (cleanDest && hdfs.exists(dest))
        clean(dest)
      if (!hdfs.exists(dest))
        hdfs.mkdirs(dest)
      for (status <- filesFrom.filterNot(_.getPath.getName.startsWith("_")))
        hdfs.rename(status.getPath, new Path(dest.toString+Consts.fileSeparator+status.getPath.getName))
    }

    // remove the source
    clean(from)
  }

}


object HadoopIntegrationJob {
  LogUtil.init
  private val log = LoggerFactory.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    if(args.length == 0) {
      log.warn("No configuration file given. \nUsage: HadoopIntegrationJob <integration-configuration-file>")
      System.exit(1)
    }
    var debug = false
    val configFile = new File(args(args.length-1))

    if(args.length>=2 && args(0)=="--debug")
      debug = true

    var config : HadoopIntegrationConfig = null
    try {
      config = HadoopIntegrationConfig.load(configFile)
    }
    catch {
      case e:ValidationException => {
        log.error("Invalid Integration Job configuration: "+e.toString +
          "\n- More details: http://www.assembla.com/code/ldif/git/nodes/ldif/ldif-core/src/main/resources/xsd/IntegrationJob.xsd")
        System.exit(1)
      }
    }

    val integrator = new HadoopIntegrationJob(config, debug)
    integrator.runIntegration()
  }
}