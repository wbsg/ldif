package ldif.modules.matching

import collection.mutable.{ArrayBuffer, Set, HashSet, Map, HashMap}
import collection.immutable.{Set => ISet}
import ldif.util.Consts
import ldif.local.runtime.CloneableQuadReader
import ldif.entity.{NodeTrait, Node}
import ldif.runtime.{QuadWriter, Triple, Quad, QuadReader}
import ldif.local.runtime.impl.{FileQuadReader, FileQuadWriter}

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

  /**
   * Flattens RDF collections to "sets", so they can be used without recursion, e.g. with plain SPARQL
   */
  def flattenUnionOf(reader: CloneableQuadReader): Map[NodeTrait, Seq[NodeTrait]] = {
    val nilNode = Node.createUriNode(Consts.RDF_NS + "nil")
    val unionOfNodes = new HashSet[NodeTrait]
    val rdfRestLinks = new HashMap[NodeTrait, NodeTrait]
    val rdfFirstValues = new HashMap[NodeTrait, NodeTrait]
    for(quad <- reader.cloneReader) {
      quad.predicate match {
        case Consts.OWL_UNIONOF => unionOfNodes.add(quad.subject); rdfRestLinks.put(quad.subject, quad.value)
        case Consts.RDFFIRST => rdfFirstValues.put(quad.subject, quad.value)
        case Consts.RDFREST => rdfRestLinks.put(quad.subject, quad.value)
        case _ => //ignore
      }
    }
    val unionOfMap = new HashMap[NodeTrait, Seq[NodeTrait]]
    for(unionOfNode <- unionOfNodes) {
      val members = new ArrayBuffer[NodeTrait]()
      var currentNode = rdfRestLinks.getOrElse(unionOfNode, nilNode)
      while(currentNode!=nilNode) {
        members.append(rdfFirstValues.get(currentNode).get)
        currentNode = rdfRestLinks.getOrElse(currentNode, nilNode)
      }
      unionOfMap.put(unionOfNode, members)
    }
    unionOfMap
  }

  def getCardinalityStatisticsOfProperty(property: String,  reader: CloneableQuadReader): Map[Int, Int] = {
    val entityMap = new HashMap[NodeTrait, Set[NodeTrait]]()
    val unionNodes = flattenUnionOf(reader)
    for(quad <- reader.cloneReader) {
      if(quad.predicate==property) {
        val nodeSet = entityMap.getOrElseUpdate(quad.subject, new HashSet[NodeTrait])
        if(quad.value.isUriNode)
          nodeSet.add(quad.value)
        else {
          for(node <- unionNodes.getOrElse(quad.value, Set()))
            nodeSet.add(node)
        }
      }
    }
    val stats = new HashMap[Int, Int]
    for((node, values) <- entityMap) {
      val size = values.size
      stats.put(size, stats.getOrElse(size, 0)+1)
    }
    stats
  }

  def flattenUnionOfForProperties(properties: ISet[String], reader: CloneableQuadReader): CloneableQuadReader = {
    val writer = new FileQuadWriter()
    val unionNodes = flattenUnionOf(reader)
    for(quad <- reader.cloneReader) {
      if(properties.contains(quad.predicate)) {
        val nodes = new HashSet[NodeTrait]
        if(unionNodes.contains(quad.value))
          for(node <- unionNodes.get(quad.value).get)
            nodes.add(node)
        else if(quad.value.isUriNode)
          nodes.add(quad.value)
        // Other blank nodes are ignored
        for(node<-nodes)
          writer.write(Triple(quad.subject, quad.predicate, node))
      } else
        writer.write(quad)
    }
    writer.finish()
    new FileQuadReader(writer)
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