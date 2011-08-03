package ldif.local.runtime

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.05.11
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */

import scala.collection.mutable.{Map, HashMap, HashSet, Set}
import ldif.local.runtime.impl.QuadQueue
import ldif.entity._
import java.util.logging.Logger
import org.w3c.dom.css.Counter

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

  def translateQuads(quadsReader: QuadReader, linkReader: QuadReader): QuadReader = {
    val uriMap = generateUriMap(linkReader)

    val quadOutput = new QuadQueue
    var counter = 0

    log.info("Start URI translation...")
    while(quadsReader.hasNext) {
      counter += 1

      quadsReader.read match {
        case Quad(s, p, o, g) => {
          val (sNew, oNew) = translateQuadURIs(s, o, uriMap)

          quadOutput.write(Quad(sNew, p, oNew, g))
        }
      }
    }
    log.info("End URI translation: Processed " + counter + " quads.")

    quadOutput
  }

  // Returns translated URI if they are found in the map, or returns the original URI
  private def translateURINode(uriNode: Node, uriMap: Map[String, String]): Node = {
    if(uriMap.contains(uriNode.value))
      Node.createUriNode(uriMap.get(uriNode.value).get, uriNode.graph)
    else
      uriNode
  }

  // Generate a Map from uri to "global" uri
  def generateUriMap(linkReader: QuadReader): Map[String, String] = {
    val uriMap = new HashMap[String, String]()

    val entityToClusterMap: Map[String, EntityCluster] = createEntityCluster(linkReader)

    for((fromURI, toURICluster) <- entityToClusterMap) {
      uriMap.put(fromURI, toURICluster.getGlobalEntity)
    }

    uriMap
  }

  private def createEntityCluster(linkReader: QuadReader): Map[String, EntityCluster] = {
    val entityToClusterMap = new HashMap[String, EntityCluster]()

//    val overAllCount = linkReader.size
    var counter = 0
    var percentCounter = 1

    while (!linkReader.isEmpty) {
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

  def extractEntityStrings(quad: Quad) = quad match { case Quad(e1, _, e2, _) => (e1.value, e2.value)}
}

case class EntityCluster(entity: String, entitySet: Set[String]) {

  var parentCluster: EntityCluster = null

  def this(entity: String) = this(entity, HashSet(entity))

  def addEntites(entities: Traversable[String]) = { entitySet ++= entities}

  // Moves all the entities to this cluster, making the other cluster obsolete
  def integrateCluster(other: EntityCluster, entityToClusterMap: Map[String, EntityCluster]) {
    other.parentCluster = this
  }

  def integrateEntity(entity: String, entityToClusterMap: Map[String, EntityCluster]) {
    entitySet += entity
    entityToClusterMap.put(entity, this)
  }

  def getGlobalEntity(): String = {
    parentCluster match {
      case null => entity
      case parent => parent.getGlobalEntity
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