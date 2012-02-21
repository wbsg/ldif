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

package ldif.local.runtime

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.05.11
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

import impl._
import scala.collection.mutable.{Map, HashMap, HashSet, Set}
import ldif.entity._
import org.slf4j.LoggerFactory
import ldif.runtime.Quad
import java.util.Properties
import java.io.{BufferedWriter, File}
import ldif.entity.entityComparator.entityComparator
import ldif.util.UriMintHelper
import ldif.runtime.QuadWriter

object URITranslator {

  private val log = LoggerFactory.getLogger(getClass.getName)

  private def translateQuadURIs(s: NodeTrait, o: NodeTrait, uriMap: Map[String, String]): (NodeTrait, NodeTrait) = {
    var sNew = s
    var oNew = o
    if (s.nodeType == Node.UriNode)
      sNew = translateURINode(s, uriMap)
    if (o.nodeType == Node.UriNode)
      oNew = translateURINode(o, uriMap)
    (sNew, oNew)
  }

  private def rewriteURIs(quadsReader: QuadReader, uriMap: Map[String, String]): QuadReader = {
    val entityGraphChecker = new EntityGraphChecker
    val file = File.createTempFile("ldif_rewritten_output", ".dat")
    file.deleteOnExit
    val quadOutput = new FileQuadWriter(file)
    var counter = 0

    log.info("Start URI translation...")
    while (quadsReader.hasNext) {
      counter += 1

      quadsReader.read match {
        case Quad(s, p, o, g) => {
          val (sNew, oNew) = translateQuadURIs(s, o, uriMap)
          if (s.nodeType == Node.UriNode)
            if (s.nodeType == Node.UriNode)
              checkAndWriteSameAsLinks(uriMap, quadOutput, entityGraphChecker, g, s, o)
          quadOutput.write(Quad(sNew, p, oNew, g))
        }
      }
    }
    log.info("End URI translation: Processed " + counter + " quads")
    quadOutput.finish
    new FileQuadReader(quadOutput.outputFile)
  }

  def translateQuads(quadsReader: QuadReader, linkReader: QuadReader, configProperties: Properties): QuadReader = {
    var uriMap: Map[String, String] = null

    var quadsReaderPassTwo = quadsReader

    val uriMinting = configProperties.getProperty("uriMinting", "false").toLowerCase=="true"
    if(uriMinting) {
      if (!quadsReader.isInstanceOf[ClonableQuadReader]) {
        quadsReaderPassTwo = quadsReader.asInstanceOf[ClonableQuadReader].cloneReader
        uriMap = generateMintedUriMap(linkReader, quadsReader, configProperties)
      } else {
        val copiedQuadsWriter = new FileQuadWriter(File.createTempFile("ldif_copyquads","queue"))
        val copyQuadsReader = new CopyQuadReader(quadsReader, copiedQuadsWriter)
        uriMap = generateMintedUriMap(linkReader, copyQuadsReader, configProperties)
        copiedQuadsWriter.finish()
        quadsReaderPassTwo = new FileQuadReader(copiedQuadsWriter.outputFile)
      }
    }
    else
      uriMap = generateUriMap(linkReader)

    rewriteURIs(quadsReaderPassTwo, uriMap)
  }

  def outputSameAsCluster(linkReader: QuadReader, output: BufferedWriter) {
    val uriMap = generateUriMap(linkReader)
    for((from, to)<-uriMap if from != to)
      output.append("<"+from+"> <" + to + ">\n")
  }

  def rewriteGlobalEntitiesWithMintedURIs(linkReader: QuadReader, mintValues: HashMap[String, String], mintingPropertiesNamespace: String): Map[String, EntityCluster] = {
    val entitiesAlreadyMinted = new HashSet[String]
    val entityToClusterMap = createEntityCluster(linkReader)
    for ((entity, cluster) <- entityToClusterMap)
      if (mintValues.contains(entity)) {
        val globalEntity = cluster.getGlobalEntity()
        if (entitiesAlreadyMinted(globalEntity))
          cluster.setGlobalEntityIfLarger(UriMintHelper.mintURI(mintingPropertiesNamespace, mintValues.get(entity).get))
        else {
          cluster.setGlobalEntity(UriMintHelper.mintURI(mintingPropertiesNamespace, mintValues.get(entity).get))
          entitiesAlreadyMinted.add(globalEntity)
        }
        mintValues.remove(entity)
      }
    entityToClusterMap
  }

  private def extractMaxMintValues(mintingPropertiesString: String, quadsReader: QuadReader, uriMintLanguageRestriction : String): HashMap[String, String] = {
    val acceptedLanguages = uriMintLanguageRestriction.split("\\s+").toSet
    val mintingProperties = Set(mintingPropertiesString.split("\\s+"): _*)
    val mintValues = new HashMap[String, String]

    while (quadsReader.hasNext) {
      val quad = quadsReader.read()
      val subject = quad.subject.value
      val value = quad.value.value
      if (mintingProperties.contains(quad.predicate)) {
        if (mintValues.contains(subject)) {
          // sort properties by the percentage of characters they have (changed from: alphanumeric sorting)
          if (entityComparator.lessThan(mintValues.get(subject).get, value))
            //if properties contain a preferred language label, use that
            if (acceptedLanguages.size>0 && quad.value.nodeType==Node.LanguageLiteral) {
              if (acceptedLanguages.contains(quad.value.datatypeOrLanguage))
                mintValues.put(subject, value)
              //else ignore
            }  else {
              mintValues.put(subject, value)
            }
        } else
          mintValues.put(subject, value)
      }
    }
    mintValues
  }

  private def generateMintedURIMap(entityToClusterMap: Map[String, EntityCluster], mintValues: HashMap[String, String], mintingPropertiesNamespace: String): Map[String, String] = {
    val sameAsURIMap = generateUriMap(entityToClusterMap)
    for ((uri, mintValue) <- mintValues) {
      if (!sameAsURIMap.contains(uri))
        sameAsURIMap.put(uri, UriMintHelper.mintURI(mintingPropertiesNamespace, mintValue))
    }
    sameAsURIMap
  }

  private def generateMintedUriMap(linkReader: QuadReader, quadsReader: QuadReader, configProperties: Properties) : Map[String, String] = {

    //set of accepted languages for minting URIs
    val uriMintLanguageRestriction = configProperties.getProperty("uriMintLanguageRestriction", "").toLowerCase

    val mintingPropertiesNamespace = configProperties.getProperty("uriMintNamespace")
    val mintingPropertiesString = configProperties.getProperty("uriMintLabelPredicate")
    if(mintingPropertiesString!=null && mintingPropertiesNamespace != null) {
      log.info("Minting URIs...")
      val mintValues: HashMap[String, String] = extractMaxMintValues(mintingPropertiesString, quadsReader, uriMintLanguageRestriction)

      val entityToClusterMap: Map[String, EntityCluster] = rewriteGlobalEntitiesWithMintedURIs(linkReader, mintValues, mintingPropertiesNamespace)

      generateMintedURIMap(entityToClusterMap, mintValues, mintingPropertiesNamespace)
    }
    else {
      log.error("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
      throw new RuntimeException("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
    }
  }

  private def checkAndWriteSameAsLinks(uriMap: Map[String, String], quadOutput: QuadWriter, entityGraphChecker: EntityGraphChecker, graph: String, nodes: NodeTrait*) {
    for(node <- nodes if node.nodeType==Node.UriNode && uriMap.contains(node.value))
      if(entityGraphChecker.addAndCheck(node.value, graph))
        writeSameAsLink(node, uriMap.get(node.value).get, graph, quadOutput)
  }

  private def writeSameAsLink(subj: NodeTrait, obj: String, graph: String, quadWriter: QuadWriter) {
    val sameAsProperty = "http://www.w3.org/2002/07/owl#sameAs"
    val o = Node.createUriNode(obj, "")
    quadWriter.write(Quad(subj, sameAsProperty, o, graph))
  }

  // Returns translated URI if they are found in the map, or returns the original URI
  private def translateURINode(uriNode: NodeTrait, uriMap: Map[String, String]): NodeTrait = {
    if(uriMap.contains(uriNode.value))
      Node.createUriNode(uriMap.get(uriNode.value).get, uriNode.graph)
    else
      uriNode
  }

  // Generate a Map from uri to "global" uri
  def generateUriMap(linkReader: QuadReader): Map[String, String] = {
    val entityToClusterMap: Map[String, EntityCluster] = createEntityCluster(linkReader)
    generateUriMap(entityToClusterMap)
  }

  private def generateUriMap(entityToClusterMap: Map[String, EntityCluster]): Map[String, String] = {
    val uriMap = new HashMap[String, String]()

    for((fromURI, toURICluster) <- entityToClusterMap) {
      val toURI = toURICluster.getGlobalEntity
      if(fromURI!=toURI)
        uriMap.put(fromURI, toURI)
    }

    uriMap
  }

  private def createEntityCluster(linkReader: QuadReader): Map[String, EntityCluster] = {
    val entityToClusterMap = new HashMap[String, EntityCluster]()

//    val overAllCount = linkReader.size
    var counter = 0
    var percentCounter = 1

    while (linkReader.hasNext) {
      counter += 1
//      if(10*counter/overAllCount > percentCounter/10) {
//        log.info("URITranslator: Links read: " + percentCounter + "%")
//        percentCounter = 100*counter/overAllCount
//      }
      val (entity1, entity2) = extractEntityStrings(linkReader.read)
      val clusterOfEntity1 = entityToClusterMap.get(entity1)
      val clusterOfEntity2 = entityToClusterMap.get(entity2)

      (clusterOfEntity1, clusterOfEntity2) match {
        case (None, None) => {
          val cluster = new EntityCluster(entity1)
          entityToClusterMap.put(entity1, cluster)
          cluster.integrateEntity(entity2, entityToClusterMap)
        }
        case (None, Some(cluster2)) => cluster2.integrateEntity(entity1, entityToClusterMap)
        case (Some(cluster1), None) => cluster1.integrateEntity(entity2, entityToClusterMap)
        case (Some(cluster1), Some(cluster2)) => {
          val globalCluster1 = cluster1.getGlobalCluster
          if (globalCluster1 != cluster2.getGlobalCluster)
            globalCluster1.integrateCluster(cluster2.getGlobalCluster(), entityToClusterMap)
        }
      }
    }

    entityToClusterMap
  }

  def extractEntityStrings(quad: Quad): Pair[String, String] = quad match { case Quad(e1, _, e2, _) => (e1.value, e2.value)}
}

case class EntityCluster(var entity: String, entitySet: Set[String]) {

  var parentCluster: EntityCluster = null

  def this(entity: String) = this(entity, HashSet(entity))

  def addEntites(entities: Traversable[String]) = { entitySet ++= entities}

  // Moves all the entities to this cluster, making the other cluster obsolete
  def integrateCluster(other: EntityCluster, entityToClusterMap: Map[String, EntityCluster]) {
    if(entityComparator.lessThan(other.entity, entity))
      other.parentCluster = this
    else
      parentCluster = other
  }

  def integrateEntity(newEntity: String, entityToClusterMap: Map[String, EntityCluster]) {
    if(entityComparator.lessThan(newEntity, entity)) {
      entitySet += newEntity
      entityToClusterMap.put(newEntity, this)
    }
    else {
      parentCluster match {
        case null => val newCluster = new EntityCluster(newEntity); entityToClusterMap.put(newEntity, newCluster);parentCluster = newCluster
        case _ => parentCluster.integrateEntity(newEntity, entityToClusterMap)
      }
    }
  }

  def getGlobalEntity(): String = {
    parentCluster match {
      case null => entity
      case parent => parent.getGlobalEntity
    }
  }

  def isGlobalCluster(): Boolean = {
    parentCluster==null
  }

  def setGlobalEntity(uri: String) {
    parentCluster match {
      case null => entity = uri
      case parent => parent.setGlobalEntity(uri)
    }
  }

  def setGlobalEntityIfLarger(uri: String) {
    parentCluster match {
      case null => if(uri > entity) entity = uri
      case parent => parent.setGlobalEntityIfLarger(uri)
    }
  }

  def getGlobalCluster(): EntityCluster = {
    parentCluster match {
      case null => this
      case parent => parent.getGlobalCluster
    }
  }

  def size = entitySet.size
}

class EntityGraphChecker {
  val entityGraphMap = new HashMap[String, Set[String]]()

  /**
   * add the graph to the entity.
   * @returns true if the graph has not been added before, else false
   */
  def addAndCheck(entity: String, graph: String): Boolean = {
    val entityGraphSet = entityGraphMap.getOrElseUpdate(entity, new HashSet[String])
    if(entityGraphSet.contains(graph))
      return false
    entityGraphSet.add(graph)
    return true
  }
}

