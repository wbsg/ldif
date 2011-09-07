package ldif.local.runtime

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.05.11
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

import impl.{MultiQuadReader, FileQuadReader, QuadQueue}
import scala.collection.mutable.{Map, HashMap, HashSet, Set}
import ldif.entity._
import java.util.logging.Logger
import org.w3c.dom.css.Counter
import java.net.URLEncoder

object URITranslator {

  private val log = Logger.getLogger(getClass.getName)

  private def translateQuadURIs(s: Node, o: Node, uriMap: Map[String, String]): (Node, Node) = {
    var sNew = s
    var oNew = o
    if (s.nodeType == Node.UriNode)
      sNew = translateURINode(s, uriMap)
    if (o.nodeType == Node.UriNode)
      oNew = translateURINode(o, uriMap)
    (sNew, oNew)
  }

  private def rewriteURIs(quadsReader: QuadReader, uriMap: Map[String, String]): QuadQueue = {
    val entityGraphChecker = new EntityGraphChecker
    val quadOutput = new QuadQueue
    var counter = 0

    log.info("Start URI translation...")
    while (quadsReader.hasNext) {
      counter += 1

      quadsReader.read match {
        case Quad(s, p, o, g) => {
          val (sNew, oNew) = translateQuadURIs(s, o, uriMap)
          if (s.nodeType == Node.UriNode)
            if (s.nodeType == Node.UriNode)
              checkAndWriteSameAsLinks(uriMap, quadOutput, entityGraphChecker, s, o)
          quadOutput.write(Quad(sNew, p, oNew, g))
        }
      }
    }
    log.info("End URI translation: Processed " + counter + " quads.")
    quadOutput
  }

  def translateQuads(quadsReader: QuadReader, linkReader: QuadReader, configProperties: ConfigProperties): QuadReader = {
    var uriMap: Map[String, String] = null

    var quadsReaderPassTwo = quadsReader

    val uriMinting = configProperties.getPropertyValue("uriMinting", "false").toLowerCase=="true"
    if(uriMinting) {
      if (!quadsReader.isInstanceOf[ClonableQuadReader])
        throw new RuntimeException("QuadReader for URI translator has to be a ClonableQuadReader.")
      quadsReaderPassTwo = quadsReader.asInstanceOf[ClonableQuadReader].cloneReader
      uriMap = generateMintedUriMap(linkReader, quadsReader, configProperties)
    }
    else
      uriMap = generateUriMap(linkReader)

    val quadOutput: QuadQueue = rewriteURIs(quadsReaderPassTwo, uriMap)

    quadOutput
  }

//  private def generateMintedUriMap(linkReader: QuadReader, quadsReader: QuadReader, configProperties: ConfigProperties): Map[String, String] = {
//    val mintingPropertiesNamespace = configProperties.getPropertyValue("uriMintNamespace")
//    val mintingPropertiesString = configProperties.getPropertyValue("uriMintLabelPredicate")
//    if(mintingPropertiesString!=null && mintingPropertiesNamespace != null) {
//      log.info("Minting URIs...")
//      val mintingProperties = Set(mintingPropertiesString.split("\\s+"): _*)
//      val entityToClusterMap = createEntityCluster(linkReader)
//      val entitiesToMint = new HashSet[String]
//      val entitiesAlreadyMinted = new HashSet[String]()
//      for(cluster <- entityToClusterMap.values)
//        if(cluster.isGlobalCluster)
//          entitiesToMint.add(cluster.entity)
//
//      while(quadsReader.hasNext) {
//        val quad = quadsReader.read()
//        val subject = quad.subject.value
//        if((entitiesToMint.contains(subject) || entitiesAlreadyMinted.contains(subject)) && mintingProperties.contains(quad.predicate)) {
//          val entityCluster = entityToClusterMap.get(subject).get
//          val mintedURI = mintURI(mintingPropertiesNamespace, quad.value.value)
//          if(entitiesAlreadyMinted.contains(subject))
//            entityCluster.setGlobalEntityIfLarger(mintedURI)
//          else {
//            entityCluster.setGlobalEntity(mintedURI)
//            entitiesToMint.remove(subject)
//            entitiesAlreadyMinted.add(subject)
//          }
//        }
//      }
//      generateUriMap(entityToClusterMap)
//    }
//    else {
//      log.severe("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
//      throw new RuntimeException("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
//    }
//  }

  def rewriteGlobalEntitiesWithMintedURIs(linkReader: QuadReader, mintValues: HashMap[String, String], mintingPropertiesNamespace: String): Map[String, EntityCluster] = {
    val entitiesAlreadyMinted = new HashSet[String]
    val entityToClusterMap = createEntityCluster(linkReader)
    for ((entity, cluster) <- entityToClusterMap)
      if (mintValues.contains(entity)) {
        val globalEntity = cluster.getGlobalEntity()
        if (entitiesAlreadyMinted(globalEntity))
          cluster.setGlobalEntityIfLarger(mintURI(mintingPropertiesNamespace, mintValues.get(entity).get))
        else {
          cluster.setGlobalEntity(mintURI(mintingPropertiesNamespace, mintValues.get(entity).get))
          entitiesAlreadyMinted.add(globalEntity)
        }
        mintValues.remove(entity)
      }
    entityToClusterMap
  }

  private def extractMaxMintValues(mintingPropertiesString: String, quadsReader: QuadReader): HashMap[String, String] = {
    val mintingProperties = Set(mintingPropertiesString.split("\\s+"): _*)
    val mintValues = new HashMap[String, String]

    while (quadsReader.hasNext) {
      val quad = quadsReader.read()
      val subject = quad.subject.value
      val value = quad.value.value
      if (mintingProperties.contains(quad.predicate)) {
        if (mintValues.contains(subject)) {
          if (mintValues.get(subject).get < value)
            mintValues.put(subject, value)
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
        sameAsURIMap.put(uri, mintURI(mintingPropertiesNamespace, mintValue))
    }
    sameAsURIMap
  }

  private def generateMintedUriMap(linkReader: QuadReader, quadsReader: QuadReader, configProperties: ConfigProperties): Map[String, String] = {
    val mintingPropertiesNamespace = configProperties.getPropertyValue("uriMintNamespace")
    val mintingPropertiesString = configProperties.getPropertyValue("uriMintLabelPredicate")
    if(mintingPropertiesString!=null && mintingPropertiesNamespace != null) {
      log.info("Minting URIs...")
      val mintValues: HashMap[String, String] = extractMaxMintValues(mintingPropertiesString, quadsReader)

      val entityToClusterMap: Map[String, EntityCluster] = rewriteGlobalEntitiesWithMintedURIs(linkReader, mintValues, mintingPropertiesNamespace)

      generateMintedURIMap(entityToClusterMap, mintValues, mintingPropertiesNamespace)
    }
    else {
      log.severe("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
      throw new RuntimeException("Missing values for uriMintNamespace and/or uriMintLabelPredicate")
    }
  }

  private def mintURI(nameSpace: String, label: String): String = {
    nameSpace + URLEncoder.encode(label.replace(' ', '_'), "UTF-8")
  }

  private def checkAndWriteSameAsLinks(uriMap: Map[String, String], quadOutput: QuadWriter, entityGraphChecker: EntityGraphChecker, nodes: Node*) {
    for(node <- nodes if node.nodeType==Node.UriNode && uriMap.contains(node.value))
      if(entityGraphChecker.addAndCheck(node.value, node.graph))
        writeSameAsLink(node, uriMap.get(node.value).get, quadOutput)
  }

  private def writeSameAsLink(subj: Node, obj: String, quadWriter: QuadWriter) {
    val sameAsProperty = "http://www.w3.org/2002/07/owl#sameAs"
    val o = Node.createUriNode(obj, "")
    quadWriter.write(Quad(subj, sameAsProperty, o, subj.graph))
  }

  // Returns translated URI if they are found in the map, or returns the original URI
  private def translateURINode(uriNode: Node, uriMap: Map[String, String]): Node = {
    if(uriMap.contains(uriNode.value))
      Node.createUriNode(uriMap.get(uriNode.value).get, uriNode.graph)
    else
      uriNode
  }

  // Generate a Map from uri to "global" uri
  private def generateUriMap(linkReader: QuadReader): Map[String, String] = {
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
            globalCluster1.integrateCluster(cluster2, entityToClusterMap)
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
    if(other.entity < entity)
      other.parentCluster = this
    else
      parentCluster = other
  }

  def integrateEntity(newEntity: String, entityToClusterMap: Map[String, EntityCluster]) {
    if(entity > newEntity) {
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
      case parent => parent.setGlobalEntity(uri)
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