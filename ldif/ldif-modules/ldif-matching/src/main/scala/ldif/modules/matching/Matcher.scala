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
import impl._
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.local.util.StringPool
import utils.SameAsAlignmentFormatConverter
import java.util.Properties
import collection.mutable.{Map, HashMap, HashSet, ArrayBuffer}
import ldif.util.{QuadUtils, CommonUtils, Consts}
import ldif.runtime.{QuadReader, Quad, QuadWriter}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/1/12
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */

object Matcher {

  def main(args: Array[String]) {
    if(args.length < 4) {
      println("Parameters: <ontology1> <ontology2> <outputFile> <pass1linkSpec> [pass2linkSpec]")
      sys.exit(1)
    }
    if(args.length>4)
      println("Running two match passes.")
    val startTime = System.currentTimeMillis()
    val ont1Reader = DumpLoader.dumpIntoFileQuadQueue(args(0))
    val ont2Reader = DumpLoader.dumpIntoFileQuadQueue(args(1))

    // Run lexical matchers
    val firstPassMatches = runFirstPassMatcher(ont1Reader, ont2Reader, args)
    var outputQueue: CloneableQuadReader = firstPassMatches
    if(args.length > 4) {
      val secondPassMatches = runSecondPassMatcher(ont1Reader.cloneReader, ont2Reader.cloneReader, outputQueue.cloneReader, args)
//      outputQueue = new MultiQuadReader(outputQueue.cloneReader, secondPassMatches)
        outputQueue = secondPassMatches
    }
    val writer = new BufferedWriter(new FileWriter(args(2)))
    outputToAlignmentFormat(outputQueue.cloneReader, writer)
//    outputURIClustersAsSameAsRDF(outputQueue, writer)
    writer.flush()
    writer.close()
    println("Finished matching after " + (System.currentTimeMillis()-startTime)/1000.0 + "s")
  }

  // This should be a high precision matcher
  private def runFirstPassMatcher(ont1Reader: CloneableQuadReader, ont2Reader: CloneableQuadReader, args: Array[String]): FileQuadReader = {
//    val firstPassMatches = new FileQuadWriter(new File("firstPassMatches.dat"))
//    if (args.length >= 4)
//      matchOntologies(ont1Reader, ont2Reader, firstPassMatches, new File(args(3)))
//    else
//      matchOntologies(ont1Reader, ont2Reader, firstPassMatches)
//    new FileQuadReader(firstPassMatches)
    new FileQuadReader(new File("firstPassMatches.dat"))
  }

  // This builds on the results of the first matcher
  private def runSecondPassMatcher(ont1Reader: CloneableQuadReader, ont2Reader: CloneableQuadReader, firstPassMatches: CloneableQuadReader, args: Array[String]): CloneableQuadReader = {
    var uriMap = getTranslationMap(firstPassMatches.cloneReader)
    val ont1ReaderRewritten = URITranslator.rewriteURIs(ont1Reader, uriMap)
    uriMap = null
    val matchedEntities = getMatchedEntities(firstPassMatches.cloneReader)
    val ont1FilteredReader = new RemoveTypesQuadReader(ont1ReaderRewritten, matchedEntities)
    val ont2FilteredReader = new RemoveTypesQuadReader(ont2Reader, matchedEntities)
    QuadUtils.dumpQuadReaderToFile(ont1FilteredReader, "mouse.nq")
    QuadUtils.dumpQuadReaderToFile(ont2FilteredReader, "human.nq")
    val secondPassMatches = new FileQuadWriter()

    matchOntologies(ont1FilteredReader, ont2FilteredReader, secondPassMatches, new File(args(4)))
    new FileQuadReader(secondPassMatches)
  }

  private def getTranslationMap(matchReader: QuadReader): Map[String, String] = {
    val mapper = new HashMap[String, String]
    for(quad <- matchReader) {
      if(quad.value.isUriNode && quad.subject.isUriNode)
        mapper.put(quad.subject.value, quad.value.value)
    }
    mapper
  }

  private def getMatchedEntities(matchReader: QuadReader): Set[String] = {
    val matchedEntities = new HashSet[String]
    for(quad <- matchReader) {
      val subject = quad.subject
      val obj = quad.value

      if(subject.isUriNode)
        matchedEntities.add(subject.value)
      if(obj.isUriNode)
        matchedEntities.add(obj.value)
    }
    matchedEntities.toSet
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
    output.finish()
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

case class RemoveTypesQuadReader(reader: QuadReader, matchedEntities: Set[String]) extends QuadReader {
  private var bufferedQuad: Quad = null
  private var counter = 0

  def size = reader.size

  def read(): Quad = {
    if(bufferedQuad==null)
      throw new RuntimeException("Error: No more quad present in queue! Use hasNext to check for present quads.")
    val returnQuad = bufferedQuad
    bufferedQuad = null
    return returnQuad
  }

  def hasNext: Boolean = {
    if(bufferedQuad!=null)
      return true

    while(reader.hasNext) {
      val quad = reader.read()
      if(!filterQuad(quad)) {
        bufferedQuad = quad
        return true
      }
    }
    return false
  }

  private def filterQuad(quad: Quad): Boolean = {
    if(quad.predicate==Consts.rdfTypeProp && matchedEntities.contains(quad.subject.value)) {
      counter += 1
      return true
    }
    else
      return false
  }
}