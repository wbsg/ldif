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
     List(new Path)
  }

  class TreeNode(node: Node) {
    private var links: List[(Node, TreeNode, Boolean)] = {}

    def setLink(propertyNode: Node, node: TreeNode, backward: Boolean): Unit = {
      links = links.add((propertyNode, node, backward))
    }
  }

  /**
   * returns the tree starting at the SUBJ node
   */
  private def constructTree(triples: List[NodeTriple]): TreeNode = {
    val nodeTable = new HashMap[Node, TreeNode]
    triples.foreach(triple => processTriple(nodeTable, triple))
    nodeTable.get(Node.)
  }

  private def processTriple(nodeTable: HashMap[Node, TreeNode], triple: NodeTriple) {
    val s = triple.getSubject
    val p = triple.getPredicate
    val o = triple.getObject

    val sNode = fetchTreeNode(s)
    val oNode = fetchTreeNode(o)

    if(s.nodeType()==NodeType.BLANKNODE || s.nodeType==NodeType.URINODE || s.nodeType==NodeType.VARIABLENODE)
      sNode.setLink(p, oNode, false)


    def fetchTreeNode(node: Node) = {
      var someNode: TreeNode = nodeTable.get(node)
      if(someNode==null) {
        someNode = new TreeNode(node)
        nodeTable.put(node, someNode)
      }
      someNode
    }
  }

  private def fetchTreeNode(node: Node, nodeTable: HashMap[Node, TreeNode]): TreeNode = {
    var someNode: TreeNode = nodeTable.get(node);
    if(someNode==null) {
      someNode = new TreeNode(node)
      nodeTable.put(node, someNode)
    }
    someNode
  }
}
