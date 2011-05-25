package ldif.local

import datasources.dump.DumpExecutor
import runtime._
import runtime.impl.{EntityQueue, QuadQueue}
import ldif.modules.silk.SilkModule
import ldif.datasources.dump.{DumpModule, DumpConfig}
import ldif.modules.silk.local.SilkLocalExecutor
import de.fuberlin.wiwiss.ldif.local.EntityBuilderExecutor
import de.fuberlin.wiwiss.ldif.{EntityBuilderModule, EntityBuilderConfig}
import java.io.{FileWriter, File}
import de.fuberlin.wiwiss.r2r._
import ldif.modules.r2r.local.R2RLocalExecutor
import ldif.modules.r2r._

import ldif.entity.{Node, EntityDescription}


object Main
{
  def main(args : Array[String])
  {
    var debug = false
//    if(args.length<1) {
//      println("No configuration file given.")
//      System.exit(1)
//    }
//    else if(args.length>=2 && args(0)=="--debug")
//      debug = true

    val configUrl = getClass.getClassLoader.getResource("ldif/local/example/test2/config.xml")
    val configFile = new File(configUrl.toString.stripPrefix("file:"))
//    val configFile = new File(args(args.length-1))

    stopWatch.getTimeSpanInSeconds
    val config = LdifConfiguration.load(configFile)
    println("Time needed to load config file: " + stopWatch.getTimeSpanInSeconds + "s")

    val dumpReader = loadDump(config.sourceDir)
    println("Time needed to load dump: " + stopWatch.getTimeSpanInSeconds + "s")
    println("Number of triples after loading the dump: " + dumpReader.size)

    if(debug==true)
      writeDebugOutput("dump", config.outputFile, dumpReader)

    val r2rReader = mapQuads(config.mappingFile, dumpReader)
    println("Time needed to build entities and map data: " + stopWatch.getTimeSpanInSeconds + "s")
    println("Number of triples after mapping the input dump: " + r2rReader.size)

    if(debug==true)
      writeDebugOutput("r2r", config.outputFile, r2rReader)

    val linkReader = generateLinks(config.linkSpecDir, r2rReader)
    println("Time needed to build entities and link data: " + stopWatch.getTimeSpanInSeconds + "s")
    println("Number of triples after linking entities: " + linkReader.size)
    writeOutput(config.outputFile, linkReader)
  }

  /**
   * Loads the dump files.
   */
  def loadDump(dumpDir : File) : QuadReader =
  {
    val dumpModule = new DumpModule(new DumpConfig(dumpDir.listFiles.map(_.getCanonicalPath)))
    val dumpExecutor = new DumpExecutor

    val dumpQuadQueue = new QuadQueue

    runInBackground
    {
      for(task <- dumpModule.tasks)
      {
        dumpExecutor.execute(task, null, dumpQuadQueue)
      }
    }

    dumpQuadQueue
  }

  /**
   * Transforms the Quads
   */
  def mapQuads(mappingFile: File, reader: QuadReader) : QuadReader = {
    val repository = new Repository(new FileOrURISource(mappingFile.getAbsolutePath))
    val executor = new R2RLocalExecutor
    val config = new R2RConfig(repository)
    val module = new R2RModule(config)

    val entityDescriptions = for(task <- module.tasks) yield task.mapping.entityDescription
    val entityReaders = buildEntities(reader, entityDescriptions.toSeq)

    val outputQueue = new QuadQueue

    runInBackground
    {
      for((r2rTask, reader) <- module.tasks.toList zip entityReaders)
        executor.execute(r2rTask, Seq(reader), outputQueue)
    }

    outputQueue
  }

  /**
   * Generates links.
   */
  def generateLinks(linkSpecDir : File, reader : QuadReader) : QuadReader =
  {
    val silkModule = SilkModule.load(linkSpecDir.listFiles.head)
    val silkExecutor = new SilkLocalExecutor

    val entityDescriptions = silkModule.tasks.toIndexedSeq.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }
    val entityReaders = buildEntities(reader, entityDescriptions)

    val outputQueue = new QuadQueue

    runInBackground
    {
      for((silkTask, readers) <- silkModule.tasks.toList zip entityReaders.grouped(2).toList)
      {
        silkExecutor.execute(silkTask, readers, outputQueue)
      }
    }

    outputQueue
  }

  //TODO we don't have an output module yet...
  def writeOutput(outputFile : File, reader : QuadReader)
  {
    val writer = new FileWriter(outputFile)
    var count = 0

    while(!reader.isEmpty)
    {
      writer.write(reader.read.toNQuadFormat + " .\n")
      count += 1
    }

    writer.close()

    println(count + " Quads written")
  }

  def writeDebugOutput(phase: String, outputFile: File, reader: QuadReader) {
    val newOutputFile = new File(outputFile.getAbsolutePath + "." + phase)
    if(!reader.isInstanceOf[QuadQueue])
      return
    val clonedReader = reader.asInstanceOf[QuadQueue].clone
    writeOutput(newOutputFile, clonedReader)
  }

  def buildEntities(reader : QuadReader, entityDescriptions : Seq[EntityDescription]) : Seq[EntityReader] =
  {
    val entityQueues = entityDescriptions.map(new EntityQueue(_))
//    for(ed <- entityDescriptions) println(ed)
    println(reader.size)

    runInBackground
    {
      val entityBuilderConfig = new EntityBuilderConfig(entityDescriptions.toIndexedSeq)
      val entityBuilderModule = new EntityBuilderModule(entityBuilderConfig)
      val entityBuilderTask = entityBuilderModule.tasks.head
      val entityBuilderExecutor = new EntityBuilderExecutor

      entityBuilderExecutor.execute(entityBuilderTask, reader, entityQueues)
    }

    entityQueues
  }

  /**
   * Evaluates an expression in the background.
   */
  private def runInBackground(function : => Unit)
  {
//    val thread = new Thread
//    {
//      override def run()
//      {
//        function
//      }
//    }
//
//    thread.start()


    function
  }
}

object stopWatch {
  private var lastTime = System.currentTimeMillis

  def getTimeSpanInSeconds(): Double = {
    val newTime = System.currentTimeMillis
    val span = newTime - lastTime
    lastTime = newTime
    span / 1000.0
  }
}