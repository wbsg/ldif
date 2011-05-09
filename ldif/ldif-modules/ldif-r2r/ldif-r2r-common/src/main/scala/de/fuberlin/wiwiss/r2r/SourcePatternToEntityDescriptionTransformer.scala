package de.fuberlin.wiwiss.r2r

import de.fuberlin.wiwiss.r2r.parser._
import ldif.entity._
import ldif.util.Uri
import scala.collection.JavaConversions._
import java.util.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 03.05.11
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */


object SourcePatternToEntityDescriptionTransformer {
  def main(args: Array[String]) : Unit = {
    transform("?s <gsdsdf> [ <nsprop> 'blah'@en ; <fsdfsd> <fsdf>; <fdsdfs> [<fsdfsd> 'fsds'^^xsd:Int ;<p4> <fd>]]")
  }

  def transform(sourcePattern: String): EntityDescription = {
    val triples: List[NodeTriple] =  Sparql2NodeTripleParser.parse(sourcePattern).toList
    triples.foreach(arg => println(arg))
    return null
  }

  def constructPaths(triples: List[NodeTriple]): List[Path] = {
     null//TODO
  }

  class TreeNode(node: Node) {
    private var links: List[(Node, TreeNode, Boolean)] = List()

    def setLink(propertyNode: Node, node: TreeNode, backward: Boolean): Unit = {
      links = (propertyNode, node, backward) :: links
    }
  }

  /**
   * returns the tree starting at the SUBJ node
   */
  private def constructTree(triples: List[NodeTriple]): TreeNode = {
    var subjNode = null
    val nodeTable = new HashMap[Node, TreeNode]
    triples.foreach(triple => processTriple(nodeTable, triple))
    nodeTable.get(Node.c)
    null
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

      if (s.nodeType() == NodeType.BLANKNODE || s.nodeType == NodeType.URINODE || s.nodeType == NodeType.VARIABLENODE)
        sNode.setLink(p, oNode, backward = false)

      if (o.nodeType() == NodeType.BLANKNODE || o.nodeType == NodeType.URINODE || o.nodeType == NodeType.VARIABLENODE)
        oNode.setLink(p, sNode, backward = false)
    }

    makeTreeNodesWithLinks
  }
}
