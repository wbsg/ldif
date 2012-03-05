package ldif.modules.matching

import ldif.local.datasources.dump.DumpLoader
import java.io.{BufferedWriter, FileWriter, File}
import ldif.modules.silk.SilkModule
import ldif.modules.silk.local.SilkLocalExecutor
import ldif.entity.EntityDescription
import ldif.local.EntityBuilderExecutor
import ldif.local.runtime._
import impl.{QuadQueue, FileEntityReader, FileEntityWriter, EntityQueue}
import ldif.util.Consts
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.local.util.StringPool
import utils.SameAsToAlignmentFormatConverter

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/1/12
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */

object Matcher {
  def main(args: Array[String]) {
    if(args.length < 3) {
      println("Parameters: <ontology1> <ontology2> <outputFile>")
      sys.exit(1)
    }
    val startTime = System.currentTimeMillis()
    val ont1Reader = DumpLoader.dumpIntoFileQuadQueue(args(0))
    val ont2Reader = DumpLoader.dumpIntoFileQuadQueue(args(1))
    val silkModule = SilkModule.load(getLinkSpecDir)
    val silkExecutor = new SilkLocalExecutor(true)
    val entityDescriptions = silkModule.tasks.toIndexedSeq.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }
    val entityReaders = buildEntities(Seq(ont1Reader, ont2Reader), entityDescriptions)
    StringPool.reset()
    val outputQueue = new QuadQueue
    for((silkTask, readers) <- silkModule.tasks.toList zip entityReaders.grouped(2).toList)
    {
      silkExecutor.execute(silkTask, readers, outputQueue)
    }
    SameAsToAlignmentFormatConverter.convert(outputQueue, args(2))
    println("Finished matching after " + (System.currentTimeMillis()-startTime)/1000.0 + "s")
  }

  private def getLinkSpecDir: File = {
    val configUrl = getClass.getClassLoader.getResource("ldif/modules/matching/resources/matchingLinkSpec")
    new File(configUrl.toString.stripPrefix("file:"))
  }

  private def writeToOutputFile(outputString: String, outputFile: File) {
    val writer = new BufferedWriter(new FileWriter(outputFile))
    writer.append(outputString)
    writer.flush()
    writer.close()
  }

  private def buildEntities(readers : Seq[QuadReader], entityDescriptions : Seq[EntityDescription]) : Seq[EntityReader] =
  {
    buildEntities(readers, entityDescriptions, new EntityBuilderExecutor())
  }

  private def buildEntities(readers : Seq[QuadReader], entityDescriptions : Seq[EntityDescription], entityBuilderExecutor : EntityBuilderExecutor, inmemory: Boolean = true) : Seq[EntityReader] =
  {
    var entityWriters: Seq[EntityWriter] = null
    val entityQueues = entityDescriptions.map(new EntityQueue(_, Consts.DEFAULT_ENTITY_QUEUE_CAPACITY))
    val fileEntityQueues = for(eD <- entityDescriptions) yield {
      val file = File.createTempFile("ldif_entities", ".dat")
      file.deleteOnExit
      new FileEntityWriter(eD, file, enableCompression = true)
    }

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
      return fileEntityQueues.map((entityWriter) => new FileEntityReader(entityWriter.entityDescription, entityWriter.inputFile, enableCompression = true ))
  }
}