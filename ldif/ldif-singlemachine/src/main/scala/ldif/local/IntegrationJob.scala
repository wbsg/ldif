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

package ldif.local

import config.IntegrationConfig
import datasources.dump.{QuadFileLoader, DumpLoader}
import runtime._
import impl._
import ldif.modules.r2r.local.R2RLocalExecutor
import ldif.modules.r2r.{R2RModule, R2RConfig}
import util.StringPool
import ldif.modules.silk.SilkModule
import ldif.modules.silk.local.SilkLocalExecutor
import ldif.modules.sieve.local.SieveLocalExecutor
import ldif.entity.EntityDescription
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import java.util.{Calendar, Properties}
import java.io._
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r.{JenaModelSource, EnumeratingURIGenerator, FileOrURISource, Repository}
import org.slf4j.LoggerFactory
import ldif.util._
import ldif.modules.sieve.{SieveConfig, EmptySieveConfig, SieveModule}

class IntegrationJob (val config : IntegrationConfig, debugMode : Boolean = false) {

  private val log = LoggerFactory.getLogger(getClass.getName)

  // Object to store all kinds of configuration data
  private var configParameters: ConfigParameters = null
  val stopWatch = new StopWatch

  var lastUpdate : Calendar = null

  def runIntegration {

    if (config.sources == null || config.sources.listFiles.size == 0)
      log.info("Integration Job skipped - No data source files found")

    else
      synchronized {
        val sourceNumber = config.sources.listFiles.size

        log.info("Integration Job started (on "+ sourceNumber +" sources)")

        stopWatch.getTimeSpanInSeconds

        // Validate configuration
        val fail = ConfigValidator.validateConfiguration(config)
        if(fail) {
          log.warn("Validation phase failed")
          sys.exit(1)
        } else {
          log.info("Validation phase succeeded in " + stopWatch.getTimeSpanInSeconds + "s")
        }

        // Quads that are not used in the integration flow, but should still be output
        val otherQuadsFile = File.createTempFile("ldif-other-quads", ".bin")
        otherQuadsFile.deleteOnExit()
        // Quads that contain external sameAs links
        val sameAsQuadsFile = File.createTempFile("ldif-sameas-quads", ".bin")
        sameAsQuadsFile.deleteOnExit

        setupConfigParameters(otherQuadsFile, sameAsQuadsFile)

        // Execute mapping phase
        val quadReaders = loadDumps(config.sources)

        var r2rReader: QuadReader = executeMappingPhase(config, quadReaders)
        if(debugMode==true)
          r2rReader = writeDebugOutput("r2r", config.outputFile, r2rReader)

        // Execute linking phase
        var linkReader: QuadReader = executeLinkingPhase(config, r2rReader)
        if(debugMode==true)
          linkReader = writeDebugOutput("silk", config.outputFile, linkReader)

        configParameters.otherQuadsWriter.finish
        val otherQuadsReader = new FileQuadReader(otherQuadsFile)
        configParameters.sameAsWriter.finish
        val sameAsReader = new FileQuadReader(sameAsQuadsFile)

        val clonedR2rReader = setupQuadReader(r2rReader)

        val allQuads = new MultiQuadReader(clonedR2rReader, otherQuadsReader)
        val allSameAsLinks = new MultiQuadReader(linkReader, sameAsReader)

        var integratedReader: QuadReader = allQuads

        if(config.properties.getProperty("rewriteURIs", "true").toLowerCase=="true")
          integratedReader = executeURITranslation(allQuads, allSameAsLinks, config.properties)

        // Execute fusion phase
//        var fusionReader: QuadReader = executeFusionPhase(config, setupQuadReader(integratedReader))
//        if(debugMode==true)
//          fusionReader = writeDebugOutput("sieve-fusion", config.outputFile, fusionReader)

        lastUpdate = Calendar.getInstance

        writeOutput(config, integratedReader)
      }
  }

  private def setupQuadReader(_clonedR2rReader: QuadReader): QuadReader = {
    var clonedR2rReader: QuadReader = _clonedR2rReader
    if (clonedR2rReader.isInstanceOf[FileQuadReader]) {
      clonedR2rReader.asInstanceOf[FileQuadReader].close()
      clonedR2rReader = new FileQuadReader(clonedR2rReader.asInstanceOf[FileQuadReader].inputFile)
    }
    clonedR2rReader
  }

  // Setup config parameters
  def setupConfigParameters(outputFile: File, sameasFile: File) {
    var otherQuads: QuadWriter = new FileQuadWriter(outputFile)
    var sameAsQuads: QuadWriter = new FileQuadWriter(sameasFile)

    configParameters = ConfigParameters(config.properties, otherQuads, sameAsQuads)

    // Setup LocalNode (to pool strings etc.)
    LocalNode.reconfigure(config.properties)
  }

  private def executeMappingPhase(config: IntegrationConfig, quadReaders: Seq[QuadReader]): QuadReader = {
    val r2rReader: QuadReader = mapQuads(config.mappingDir, quadReaders)
    log.info("Time needed to map data: " + stopWatch.getTimeSpanInSeconds + "s")

    r2rReader
  }

  private def executeLinkingPhase(config: IntegrationConfig, r2rReader: QuadReader): QuadReader = {
    val linkReader = generateLinks(config.linkSpecDir, r2rReader)
    log.info("Time needed to link data: " + stopWatch.getTimeSpanInSeconds + "s")
    log.info("Number of links generated by silk: " + linkReader.size)
    linkReader
  }

    private def executeFusionPhase(config: IntegrationConfig, inputQuadsReader: QuadReader): QuadReader = {
        val sieveFusionReader = fuseQuads(config.sieveSpecDir, inputQuadsReader)
        log.info("Time needed to fuse data: " + stopWatch.getTimeSpanInSeconds + "s")
        log.info("Number of entities fused by sieve: " + sieveFusionReader.size)
        sieveFusionReader
    }

  private def executeURITranslation(inputQuadReader: QuadReader, linkReader: QuadReader, configProperties: Properties): QuadReader = {
    val integratedReader = URITranslator.translateQuads(inputQuadReader, linkReader, configProperties)

    log.info("Time needed to translate URIs: " + stopWatch.getTimeSpanInSeconds + "s")
    integratedReader
  }

  /**
   * Loads the dump files.
   */
  private def loadDumps(sources : File) : Seq[QuadReader] =
  {
    val discardFaultyQuads = config.properties.getProperty("discardFaultyQuads", "false").toLowerCase=="true"
    if(sources.isDirectory) {
      val quadQueues =
        for (dump <- sources.listFiles) yield {
          val quadQueue = new BlockingQuadQueue(Consts.DEFAULT_QUAD_QUEUE_CAPACITY)
          runInBackground
          {
            val inputStream = DumpLoader.getFileStream(dump)
            val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
            val quadParser = new QuadFileLoader(dump.getName, discardFaultyQuads)
            quadParser.readQuads(bufferedReader, quadQueue)
            quadQueue.finish
          }
          quadQueue
        }
      quadQueues.toSeq
    }
    else Seq.empty[QuadReader]
  }

  /**
   * Transforms the Quads
   */
  private def mapQuads(mappingDir: File, readers: Seq[QuadReader]) : QuadReader = {
    val mappingSource = new FileOrURISource(mappingDir)
    val uriGenerator = new EnumeratingURIGenerator("http://www4.wiwiss.fu-berlin.de/ldif/imported", BigInteger.ONE);
    val importedMappingModel = Repository.importMappingDataFromSource(mappingSource, uriGenerator)
    val repository = new Repository(new JenaModelSource(importedMappingModel))
    val executor = new R2RLocalExecutor
    val config = new R2RConfig(repository)
    val module = new R2RModule(config)

    val entityDescriptions = for(task <- module.tasks) yield task.mapping.entityDescription
    val entityReaders = buildEntities(readers, entityDescriptions.toSeq, configParameters)
//     log.info("Memory used (after build entities): " + MemoryUsage.getMemoryUsage() +" MB")   //TODO: remove
    StringPool.reset
//     log.info("Memory used (after resetting StringPool): " + MemoryUsage.getMemoryUsage() +" MB")   //TODO: remove
    log.info("Time needed to load dump and build entities for mapping phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val outputFile = File.createTempFile("ldif-mapped-quads", ".bin")
    outputFile.deleteOnExit
    val writer = new FileQuadWriter(outputFile)

    //runInBackground
    {
      for((r2rTask, reader) <- module.tasks.toList zip entityReaders)
        executor.execute(r2rTask, Seq(reader), writer)
    }
    writer.finish
    new FileQuadReader(outputFile)
  }

  /**
   * Generates links.
   */
  private def generateLinks(linkSpecDir : File, reader : QuadReader) : QuadReader =
  {
    val silkModule = SilkModule.load(linkSpecDir)
    val inmemory = config.properties.getProperty("entityBuilderType", "in-memory")=="in-memory"
    val silkExecutor = if(inmemory)
        new SilkLocalExecutor
      else
        new SilkLocalExecutor(true)

    val entityDescriptions = silkModule.tasks.toIndexedSeq.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }
    val entityReaders = buildEntities(Seq(reader), entityDescriptions, ConfigParameters(config.properties))
    StringPool.reset
    log.info("Time needed to build entities for linking phase: " + stopWatch.getTimeSpanInSeconds + "s")

    val outputQueue = new QuadQueue

    //runInBackground
    {
      for((silkTask, readers) <- silkModule.tasks.toList zip entityReaders.grouped(2).toList)
      {
        silkExecutor.execute(silkTask, readers, outputQueue)
      }
    }

    outputQueue
  }

    /**
     * Performs data fusion
     */
  private def fuseQuads(sieveSpecDir : File, inputQuadsReader : QuadReader) : QuadReader =
  {
    val sieveModule = SieveModule.load(sieveSpecDir)

    sieveModule.config.sieveConfig match {
      case e: EmptySieveConfig => {
        log.info("[FUSION] No Sieve configuration found. No fusion will be performed.")
        val echo = new QuadQueue()
        inputQuadsReader.foreach(q => echo.write(q));
        return echo;
      }
      case c: SieveConfig => {
        log.debug("Sieve will perform fusion, config=%s.".format(sieveSpecDir.getAbsolutePath))
        val inMemory = config.properties.getProperty("entityBuilderType", "in-memory")=="in-memory"
        val sieveExecutor = new SieveLocalExecutor

        val entityDescriptions = sieveModule.tasks.toIndexedSeq.map(sieveExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }


        val entityReaders = buildEntities(Seq(inputQuadsReader), entityDescriptions, ConfigParameters(config.properties))

        StringPool.reset
        log.info("[FUSION] Time needed to build entities for fusion phase: " + stopWatch.getTimeSpanInSeconds + "s")

        val outputQueue = new QuadQueue

          //runInBackground
        {
          for((sieveTask, readers) <- sieveModule.tasks.toList zip entityReaders.grouped(2).toList)
          {
            sieveExecutor.execute(sieveTask, readers, outputQueue)
          }
        }

        outputQueue
      }
    }


  }

  /**
   * Build Entities.
   */
  private def buildEntities(readers : Seq[QuadReader], entityDescriptions : Seq[EntityDescription], configParameters: ConfigParameters) : Seq[EntityReader] =
  {
    var entityWriters: Seq[EntityWriter] = null
    val entityQueues = entityDescriptions.map(new EntityQueue(_, Consts.DEFAULT_ENTITY_QUEUE_CAPACITY))
    val fileEntityQueues = for(eD <- entityDescriptions) yield {
      val file = File.createTempFile("ldif_entities", ".dat")
      file.deleteOnExit
      new FileEntityWriter(eD, file)
    }

    val inmemory = config.properties.getProperty("entityBuilderType", "in-memory")=="in-memory"

    //Because of memory problems circumvent with FileQuadQueue */
    if(inmemory)
      entityWriters = entityQueues
    else
      entityWriters = fileEntityQueues

    try
    {
      val entityBuilderConfig = new EntityBuilderConfig(entityDescriptions.toIndexedSeq)
      val entityBuilderModule = new EntityBuilderModule(entityBuilderConfig)
      val entityBuilderTask = entityBuilderModule.tasks.head
      val entityBuilderExecutor = new EntityBuilderExecutor(configParameters)

      entityBuilderExecutor.execute(entityBuilderTask, readers, entityWriters)
    } catch {
      case e: Throwable => {
        e.printStackTrace
        sys.exit(2)
      }
    }

    if(inmemory)
      return entityQueues
    else
      return fileEntityQueues.map((entityWriter) => new FileEntityReader(entityWriter.entityDescription, entityWriter.inputFile))
  }

  /**
   * Evaluates an expression in the background.
   */
  private def runInBackground(function : => Unit) {
    val thread = new Thread {
      private val listener: FatalErrorListener = FatalErrorListener

      override def run {
        try {
          function
        } catch {
          case e: Exception => listener.reportError(e)
        }
      }
    }
    thread.start
  }

  //TODO we don't have an output module, yet...
  private def writeOutput(config: IntegrationConfig, reader : QuadReader)    {
    val writer = new FileWriter(config.outputFile)
    var count = 0
    val nqOutput = config.properties.getProperty("outputFormat", "nq").toLowerCase.equals("nq")

    while(reader.hasNext) {
      if(nqOutput)
        writer.write(reader.read().toNQuadFormat + " .\n")
      else
        writer.write(reader.read().toNTripleFormat + " .\n")
      count += 1
    }

    writer.close
    log.info(count + " Quads written")
  }

  private def writeDebugOutput(phase: String, outputFile: File, reader: QuadReader): QuadReader = {
    val newOutputFile = new File(outputFile.getAbsolutePath + "." + phase)
    copyAndDumpQuadQueue(reader, newOutputFile.getAbsolutePath)
  }

  def copyAndDumpQuadQueue(quadQueue: QuadReader, outputFile: String): QuadReader = {
    val quadOutput = File.createTempFile("ldif-debug-quads", ".bin")
    quadOutput.deleteOnExit
    val writer = new FileQuadWriter(quadOutput)
    val quadWriter = new BufferedWriter(new FileWriter(outputFile))

    while(quadQueue.hasNext) {
      val next = quadQueue.read
      quadWriter.write(next.toNQuadFormat)
      quadWriter.write(" .\n")
      writer.write(next)
    }
    quadWriter.flush()
    quadWriter.close()
    writer.finish
    return new FileQuadReader(writer.outputFile)
  }
}


object IntegrationJob {
  LogUtil.init
  private val log = LoggerFactory.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    if(args.length == 0) {
      log.warn("Usage: IntegrationJob <integration job config file>")
      System.exit(-1)
    }
    var debug = false
    val configFile = new File(args(args.length-1))

    if(args.length>=2 && args(0)=="--debug")
      debug = true

    val integrator = new IntegrationJob(IntegrationConfig.load(configFile), debug)
    integrator.runIntegration
  }
}





