package ldif.modules.matching

import collection.mutable.{ArrayBuffer, Set, HashSet, Map, HashMap}
import ldif.util.Consts
import ldif.local.runtime.CloneableQuadReader
import ldif.entity.{NodeTrait, Node}
import ldif.runtime.{QuadWriter, Triple, Quad, QuadReader}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 5/29/12
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */

object StructuralFeatureExtractor {
  /**
   * Builds the hierarchy and returns the root(s) of it
   */
  def buildHierarchy(reader: CloneableQuadReader): Set[HierarchyNode] = {
    val roots = new HashSet[HierarchyNode]
    val nodes = new HashMap[NodeTrait, HierarchyNode]
    val subClassOf: Map[NodeTrait, NodeTrait] = new HashMap[NodeTrait, NodeTrait]
    for(quad <- reader.cloneReader) {
      // Save subClassOf relations
      if(quad.predicate==Consts.RDFS_SUBCLASSOF && quad.value.isUriNode && quad.subject.isUriNode && subClassOf.get(quad.subject)==None)
        subClassOf.put(quad.subject, quad.value)
      // Save Class nodes
      if(quad.predicate==Consts.RDFTYPE_URI && quad.value.value==Consts.OWL_CLASS && quad.subject.isUriNode && nodes.get(quad.subject)==None)
        nodes.put(quad.subject, HierarchyNode(quad.subject, new HashSet[HierarchyNode]))
    }
    for(node <- nodes.keySet) {
      subClassOf.get(node) match {
        case None =>
        case Some(parent) => nodes.get(parent).get.children.add(nodes.get(node).get)
      }
    }
    for(node <- nodes.keySet if getHierarchyLevel(node, subClassOf, Some(0))==0)
      roots.add(nodes.get(node).get)

    roots
  }

  def extractStructuralFeatures(roots: Set[HierarchyNode], writer: QuadWriter) {
    for(root <- roots)
      recursiveExtract(root, writer, 0)
  }

  def recursiveExtract(node: HierarchyNode, writer: QuadWriter, level: Int): Int = {
    for(quad <- node.toQuads)
      writer.write(quad)
    writer.write(Triple(node.value, Consts.LDIF_numberOfChildren, Node.createLiteral(node.children.size.toString)))
    writer.write(Triple(node.value, Consts.LDIF_hierarchyLevel, Node.createLiteral(level.toString)))
    var number = 1
    for(child <- node.children) {
      writer.write(Triple(child.value, Consts.RDFS_SUBCLASSOF, node.value));
      number += recursiveExtract(child, writer, level+1)
    }
    writer.write(Triple(node.value, Consts.LDIF_sizeOfSubTree, Node.createLiteral(number.toString)))
    number
  }

  def getHierarchyLevel(node: NodeTrait, subClassOf: Map[NodeTrait, NodeTrait], maxLevel: Option[Int] = None): Int = {
    if(!subClassOf.contains(node))
      return 0
    else {
      if(maxLevel==Some(0)) // Return one higher than max if max is reached
        return 1
      if(maxLevel==None)
        return getHierarchyLevel(subClassOf.get(node).get, subClassOf) + 1
      else
        return getHierarchyLevel(subClassOf.get(node).get, subClassOf, Some(maxLevel.get-1))
    }
  }
}

case class HierarchyNode(val value: NodeTrait, val children: Set[HierarchyNode]) {
  val OWL_CLASS = Node.createUriNode(Consts.OWL_CLASS)

  def toQuads: Seq[Quad] = {
    val quads = new ArrayBuffer[Quad]
    quads.append(Triple(value, Consts.RDFTYPE_URI, OWL_CLASS))
    return quads
  }
}