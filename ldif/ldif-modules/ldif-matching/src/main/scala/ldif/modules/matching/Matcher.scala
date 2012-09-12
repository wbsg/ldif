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
import ldif.local.EntityBuilderExecutor
import ldif.local.runtime._
import impl._
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import ldif.local.util.StringPool
import utils.SameAsAlignmentFormatConverter
import java.util.Properties
import collection.mutable.{Map, HashMap, HashSet, ArrayBuffer}
import ldif.util.{TemporaryFileCreator, QuadUtils, CommonUtils, Consts}
import ldif.runtime.{QuadReader, Quad, QuadWriter, Triple}
import ldif.entity.{Node, EntityDescription}
import java.util.logging.{LogManager, Level, Logger}
import java.util.prefs.Preferences

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/1/12
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */

object Matcher {
  val removeAlreadyMatchedConceptsFromOntologyA = true;
  val removeAlreadyMatchedConceptsFromOntologyB = false;
  val rewriteURIsOfMatchedConceptsInOntologyA = false;

  def main(args: Array[String]) {
    LogManager.getLogManager.readConfiguration()

    if(args.length < 4) {
      println("Parameters: <ontology1> <ontology2> <outputFile> <pass1matchSpec> [more matchSpecs .. ]")
      sys.exit(1)
    }
    if(args.length>4)
      println("Running " + (args.length-3) + " match passes.")
    val startTime = System.currentTimeMillis()
    val ont1Reader = DumpLoader.dumpIntoFileQuadQueue(args(0))
    val ont2Reader = DumpLoader.dumpIntoFileQuadQueue(args(1))
//    val structureProps = Set(Consts.RDFS_DOMAIN, Consts.RDFS_RANGE, Consts.RDFS_SUBCLASSOF)
//    val ont1Reader = StructuralFeatureExtractor.flattenUnionOfForProperties(structureProps, ont1ReaderTemp)
//    val ont2Reader = StructuralFeatureExtractor.flattenUnionOfForProperties(structureProps, ont2ReaderTemp)
//    println("\nRDFS Domain:")
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_DOMAIN, ont1Reader))
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_DOMAIN, ont2Reader))
//    println("\nRDFS Range:")
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_RANGE, ont1Reader))
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_RANGE, ont2Reader))
//    println("\nRDFS subClassOf:")
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_SUBCLASSOF, ont1Reader))
//    println(StructuralFeatureExtractor.getCardinalityStatisticsOfProperty(Consts.RDFS_SUBCLASSOF, ont2Reader))
//    return

//    QuadUtils.dumpQuadReaderToFile(test1, "hierarchy1.nt", true)
//    QuadUtils.dumpQuadReaderToFile(test2, "hierarchy2.nt", true)
//    val outputQueue =  runFirstPassMatcher(test1, test2, "/home/andreas/projects/ldif/ldif/ldif-modules/ldif-matching/src/main/resources/ldif/modules/matching/resources/matchingLinkSpec/structMatch.xml")

//    return
//
    // Run lexical matchers
    val firstPassMatches = runFirstPassMatcher(ont1Reader, ont2Reader, args(3))
    Logger.getLogger("de.fuberlin.wiwiss.silk.util.task.HasStatus").setLevel(Level.OFF)
    println("First matcher found: " + firstPassMatches.size + " matches")
    var outputQueue: CloneableQuadReader = firstPassMatches
//    if(outputQueue.size==0) {
//      val structureOnt1 = new QuadQueue
//      val structureOnt2 = new QuadQueue
//      val hierarchy1 = StructuralFeatureExtractor.buildHierarchy(ont1Reader)
//      val hierarchy2 = StructuralFeatureExtractor.buildHierarchy(ont2Reader)
//      StructuralFeatureExtractor.extractStructuralFeatures(hierarchy1, structureOnt1)
//      StructuralFeatureExtractor.extractStructuralFeatures(hierarchy2, structureOnt2)
//      val structureMatches = runNextPassMatcher(structureOnt1, structureOnt2, outputQueue.cloneReader, "/home/andreas/projects/ldif/ldif/ldif-modules/ldif-matching/src/main/resources/ldif/modules/matching/resources/matchingLinkSpec/structMatch.xml")
//      println("Structural matcher found " + structureMatches.size + " matches")
//      outputQueue = structureMatches
//    }
    for(i <- 4 until args.length) {
      val nextPassMatches = if(args(i).contains("augmented")) {
        val augmented1 = new MultiQuadReader(ont1Reader.cloneReader, augmentInstancesWithRDFSLabel(ont1Reader))
        val augmented2 = new MultiQuadReader(ont2Reader.cloneReader, augmentInstancesWithRDFSLabel(ont2Reader))
        runNextPassMatcher(augmented1, augmented2, outputQueue.cloneReader, args(i))
      }
      else
        runNextPassMatcher(ont1Reader.cloneReader, ont2Reader.cloneReader, outputQueue.cloneReader, args(i))
      println("\nMatcher " + (i-2) + " found: " + nextPassMatches.size + " matches.\n")
  //      outputQueue = new MultiQuadReader(outputQueue.cloneReader, secondPassMatches)
      outputQueue = new MultiQuadReader(nextPassMatches, outputQueue.cloneReader)
  //        QuadUtils.dumpQuadReaderToFile(outputQueue, "test.nt", true)
    }
    val writer = new BufferedWriter(new FileWriter(args(2)))
//    outputToAlignmentFormat(outputQueue.cloneReader, writer)
//    outputToAlignmentFormatTTL(outputQueue.cloneReader, writer)
//    outputURIClustersAsSameAsRDF(outputQueue, writer)
    outputToSameAsLinks(outputQueue, writer)
    writer.flush()
    writer.close()
    println("Finished matching after " + (System.currentTimeMillis()-startTime)/1000.0 + "s")
  }

  private def augmentInstancesWithRDFSLabel(ont: CloneableQuadReader): QuadReader = {
    val rdfsLabels = new QuadQueue
    val entitySet = new HashSet[String]
    for(quad <- ont.cloneReader) {
      if(quad.subject.isUriNode && quad.predicate==Consts.RDFTYPE_URI && Set(Consts.OWL_DATATYPEPROPERTY, Consts.OWL_OBJECTPROPERTY, Consts.OWL_CLASS).contains(quad.value.value))
        entitySet.add(quad.subject.value)
    }
    val sortedEntities = entitySet.toSeq.sortWith(_.toLowerCase < _.toLowerCase)
    var lastPrefix = "noprefix"
    for(entity <- sortedEntities) {
      if(entity.toLowerCase.startsWith(lastPrefix.toLowerCase))
        rdfsLabels.write(Triple(Node.createUriNode(entity), Consts.RDFS_LABEL, Node.createLiteral(entity.substring(lastPrefix.length()))))
      else
        lastPrefix = entity
    }
    println(rdfsLabels.size)
    rdfsLabels
  }

  // This should be a high precision matcher
  private def runFirstPassMatcher(ont1Reader: CloneableQuadReader, ont2Reader: CloneableQuadReader, matchSpec: String): FileQuadReader = {
//    val firstPassMatches = new FileQuadWriter(new File("firstPassMatches.dat"))
    val firstPassMatches = new FileQuadWriter()
    matchOntologies(ont1Reader, ont2Reader, firstPassMatches, new File(matchSpec))
    new FileQuadReader(firstPassMatches)
//    new FileQuadReader(new File("firstPassMatches.dat"))
  }

  // This builds on the results of the first matcher
  private def runNextPassMatcher(ont1Reader: CloneableQuadReader, ont2Reader: CloneableQuadReader, firstPassMatches: CloneableQuadReader, matchSpec: String): CloneableQuadReader = {
    var uriMap = getTranslationMap(firstPassMatches.cloneReader)
    val ont1ReaderRewritten = if (rewriteURIsOfMatchedConceptsInOntologyA) URITranslator.rewriteURIs(ont1Reader, uriMap) else ont1Reader
    uriMap = null
    val matchedEntities = getMatchedEntities(firstPassMatches.cloneReader)
    val ont1FilteredReader = if (removeAlreadyMatchedConceptsFromOntologyA) new RemoveTypesQuadReader(ont1ReaderRewritten, matchedEntities) else ont1ReaderRewritten
    val ont2FilteredReader = if (removeAlreadyMatchedConceptsFromOntologyB) new RemoveTypesQuadReader(ont2Reader, matchedEntities) else ont2Reader
//    QuadUtils.dumpQuadReaderToFile(ont1FilteredReader, "mouse.nt", asTriples = true)
//    QuadUtils.dumpQuadReaderToFile(ont2FilteredReader, "human.nt", asTriples = true)
    val secondPassMatches = new FileQuadWriter()

    matchOntologies(ont1FilteredReader, ont2FilteredReader, secondPassMatches, new File(matchSpec))
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

  private def outputToSameAsLinks(matches: QuadReader, writer: BufferedWriter) {
    println("Writing " + matches.size + " matches.")
    for(quad <- matches)
      writer.append(quad.toNTripleFormat).append(" .\n")
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
      val file = TemporaryFileCreator.createTemporaryFile("ldif_entities", ".dat")
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

