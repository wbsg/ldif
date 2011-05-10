package de.fuberlin.wiwiss.r2r

import de.fuberlin.wiwiss.r2r.parser._
import de.fuberlin.wiwiss.r2r.parser.NodeType._
import ldif.entity.{EntityDescription, Path, Restriction, PathOperator}
import ldif.entity.Restriction._
import ldif.entity.{BackwardOperator, ForwardOperator}
import ldif.util.Uri
import scala.collection.JavaConversions._
import java.util.HashMap
import collection.immutable.List._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */


object SourcePatternToEntityDescriptionTransformer {
  def main(args: Array[String]) : Unit = {
    val (entityDescription, variableToIndexMap) = transform(args(0), args.slice(1,args.length).toList, new PrefixMapper)
    printEntityDescription(entityDescription)
  }

  def printEntityDescription(entityDescription: EntityDescription) {
    println("Paths:")
    for(pattern <- entityDescription.patterns(0)) {
      println("  " + pattern)
    }
    println("Restrictions:")
    println("  " + entityDescription.restriction)
  }

  def transform(sourcePattern: String, variableDependencies: List[String], prefixMapper: PrefixMapper): (EntityDescription, Map[String, Int]) = {
    val triples: List[NodeTriple] =  Sparql2NodeTripleParser.parse(sourcePattern, prefixMapper).toList
    val (paths, operators) = constructPathsAndRestrictions(triples, variableDependencies)
    var index = 0
    var variableToIndexMap: Map[String, Int] = Map()
    var pattern: List[Path] = List()
    for((variableValue ,path) <- paths) {
      variableToIndexMap += (variableValue -> index)
      index += 1
      pattern = path :: pattern
    }
    var restriction = Restriction(None)
    if(operators.size>0)
      restriction = Restriction(Some(And(operators)))
    (EntityDescription(restriction, IndexedSeq(pattern.toIndexedSeq)), variableToIndexMap)
  }

  def constructPathsAndRestrictions(triples: List[NodeTriple], variableDependencies: List[String]): (Map[String, Path],
      List[Operator]) = {
    var paths: Map[String, Path] = Map()
    var restrictions: List[Operator] = List()

    val subjRoot = constructTree(triples)
    if(subjRoot==null) throw new R2RException("No SUBJ variable in Source Pattern")

    def recursiveConstruct(node: TreeNode, path: List[PathOperator], visited: Set[TreeNode]) {
      val parsedNode = node.getNode
      val nodeType = parsedNode.nodeType
      var added = false

      if(nodeType==VARIABLENODE && variableDependencies.contains(parsedNode.value) && parsedNode.value!="SUBJ") {
        paths += (parsedNode.value -> Path("SUBJ", path))
        added = true
      }

      val links = node.getLinks
      val linked = hasLinks(links, visited)

      if(!linked) {
        if((nodeType==VARIABLENODE && !added) || nodeType==BLANKNODE)
          restrictions = Exists(Path("SUBJ",path)) :: restrictions
        else if(!added)//TODO: Have to differentiate between different types (lang literal, datatype literal etc.)
          restrictions = Condition(Path("SUBJ",path), Set(parsedNode.value)) :: restrictions
      } else {
        links.foreach{case (propertyNode, nextNode, backward) =>
          if(visited.contains(nextNode))
            return
          val visitedNew = visited + nextNode

          if(backward)
            recursiveConstruct(nextNode, path ++ List(BackwardOperator(propertyNode.value)), visitedNew)
          else
            recursiveConstruct(nextNode, path ++ List(ForwardOperator(propertyNode.value)), visitedNew)
        }
      }
    }

    def hasLinks(links: List[(Node, TreeNode, Boolean)], visited: Set[TreeNode]): Boolean = {
      var count = 0
      links.foreach{case (propertyNode, nextNode, backward) =>
        if(!visited.contains(nextNode))
          count += 1
      }
      count > 0
    }

    recursiveConstruct(subjRoot, List(), Set(subjRoot))
    (paths, restrictions)
  }

  class TreeNode(node: Node) {
    private var links: List[(Node, TreeNode, Boolean)] = List()

    def setLink(propertyNode: Node, node: TreeNode, backward: Boolean): Unit = {
      links = (propertyNode, node, backward) :: links
    }

    def getNode = node
    def getLinks = links
  }

  /**
   * returns the tree starting at the SUBJ node
   */
  private def constructTree(triples: List[NodeTriple]): TreeNode = {
    val nodeTable = new HashMap[Node, TreeNode]
    triples.foreach(triple => processTriple(nodeTable, triple))
    nodeTable.get(Node.createVariableNode("SUBJ"))
  }

  private def processTriple(nodeTable: HashMap[Node, TreeNode], triple: NodeTriple) {
    def fetchTreeNode(node: Node) = {
      var treeNode: TreeNode = nodeTable.get(node)
      if(treeNode==null) {
        treeNode = new TreeNode(node)
        nodeTable.put(node, treeNode)
      }
      treeNode
    }

    def makeTreeNodesWithLinks: Unit = {
      val s = triple.getSubject
      val p = triple.getPredicate
      val o = triple.getObject

      val sNode = fetchTreeNode(s)
      val oNode = fetchTreeNode(o)

      if (s.nodeType() == BLANKNODE || s.nodeType == URINODE || s.nodeType == VARIABLENODE)
        sNode.setLink(p, oNode, backward = false)

      if (o.nodeType() == BLANKNODE || o.nodeType == URINODE || o.nodeType == VARIABLENODE)
        oNode.setLink(p, sNode, backward = true)
    }

    makeTreeNodesWithLinks
  }
}
