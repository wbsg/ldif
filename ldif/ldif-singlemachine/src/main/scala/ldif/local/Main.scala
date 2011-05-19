package ldif.local

import datasources.dump.DumpExecutor
import runtime._
import runtime.impl.{EntityQueue, QuadQueue}
import ldif.modules.silk.SilkModule
import ldif.datasources.dump.{DumpModule, DumpConfig}
import ldif.modules.silk.local.SilkLocalExecutor
import de.fuberlin.wiwiss.ldif.local.EntityBuilderExecutor
import ldif.entity.EntityDescription
import de.fuberlin.wiwiss.ldif.{EntityBuilderModule, EntityBuilderConfig}
import java.io.{FileWriter, File}

object Main
{
  def main(args : Array[String])
  {
    val configUrl = getClass.getClassLoader.getResource("ldif/local/example/config.xml")
    val configFile = new File(configUrl.toString.stripPrefix("file:"))
    val config = LdifConfiguration.load(configFile)

    val dumpReader = loadDump(config.sourceDir)
    val linkReader = generateLinks(config.linkSpecDir, dumpReader)
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
      writer.write(reader.read + "\n")
      count += 1
    }

    writer.close()

    println(count + " Quads written")
  }

  def buildEntities(reader : QuadReader, entityDescriptions : Seq[EntityDescription]) : Seq[EntityReader] =
  {
    val entityQueues = entityDescriptions.map(new EntityQueue(_))

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
//
//    while(thread.isAlive && !waitFor) Thread.sleep(100)

    //TODO at the moment everything is run sequential because there is no way to detect if a queue is still being written
    function
  }
}