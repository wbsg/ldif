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
import utils.SameAsAlignmentFormatConverter
import collection.mutable.ArrayBuffer
import java.util.Properties
import ldif.runtime.QuadWriter

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
      println("Parameters: <ontology1> <ontology2> <outputFile> [linkSpec]")
      sys.exit(1)
    }
    val startTime = System.currentTimeMillis()
    val ont1Reader = DumpLoader.dumpIntoFileQuadQueue(args(0))
    val ont2Reader = DumpLoader.dumpIntoFileQuadQueue(args(1))
    val outputQueue = new QuadQueue
    if(args.length==4)
      matchOntologies (ont1Reader, ont2Reader, outputQueue, new File(args(3)))
    else
      matchOntologies (ont1Reader, ont2Reader, outputQueue)
    val writer = new BufferedWriter(new FileWriter(args(2)))
    outputToAlignmentFormat(outputQueue, writer)
//    outputURIClustersAsSameAsRDF(outputQueue, writer)
    writer.flush()
    writer.close()
    println("Finished matching after " + (System.currentTimeMillis()-startTime)/1000.0 + "s")
  }



  private def outputURIClustersAsSameAsRDF(matches: QuadReader, writer: BufferedWriter) {
    URITranslator.outputSameAsCluster(matches, writer)
  }

  private def outputToAlignmentFormat(matches: QuadReader, writer: BufferedWriter) {
    SameAsAlignmentFormatConverter.convertToAlignmentFormat(matches, writer)
  }

  private def outputToAlignmentFormatTTL(matches: QuadReader, writer: BufferedWriter) {
    SameAsAlignmentFormatConverter.convertToAlignmentFormat(matches, writer, false)
  }

  def matchOntologies(ont1Reader: QuadReader,  ont2Reader: QuadReader, output: QuadWriter, matchSpec: File = getLinkSpecDir) {
    val silkModule = SilkModule.load(matchSpec)
    val silkExecutor = new SilkLocalExecutor(useFileInstanceCache = true, allowLinksForSameURIs = true)
    val entityDescriptions = silkModule.tasks.toIndexedSeq.map(silkExecutor.input).flatMap{ case StaticEntityFormat(ed) => ed }
    val entityReaders1 = buildEntities(Seq(ont1Reader), entityDescriptions.zipWithIndex.filter(a => a._2 % 2 == 0).map(a=>a._1))
    val entityReaders2 = buildEntities(Seq(ont2Reader), entityDescriptions.zipWithIndex.filter(a => a._2 % 2 == 1).map(a=>a._1))
    val entityReaders = mergeReaders(entityReaders1, entityReaders2)
    StringPool.reset()

    for((silkTask, readers) <- silkModule.tasks.toList zip entityReaders.grouped(2).toList)
    {
      silkExecutor.execute(silkTask, readers, output)
    }
  }

  private def mergeReaders(readers1: Seq[EntityReader], readers2: Seq[EntityReader]): Seq[EntityReader] = {
    val result = new ArrayBuffer[EntityReader]()
    for ((a,b) <- readers1 zip readers2) {
      result.append(a)
      result.append(b)
    }
    result
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
    val properties = new Properties()
//    properties.put("entityBuilderType", "quad-store")
    buildEntities(readers, entityDescriptions, new EntityBuilderExecutor(ConfigParameters(properties)), properties)
  }

  private def buildEntities(readers : Seq[QuadReader], entityDescriptions : Seq[EntityDescription], entityBuilderExecutor : EntityBuilderExecutor, properties: Properties) : Seq[EntityReader] =
  {
    val inmemory = properties.getProperty("entityBuilderType", "inmemory")=="inmemory"
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
      return fileEntityQueues.map((entityWriter) => new FileEntityReader(entityWriter))
  }


}