/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local

import ldif.entity._
import org.slf4j.LoggerFactory
import ldif.entity.Restriction._
import collection.mutable.{ArraySeq, ArrayBuffer, HashMap, Set, HashSet}
import actors.{Future, Futures}
import scala.collection.JavaConversions._
import ldif.local.util.StringPool
import ldif.local.runtime.{LocalNode, EntityWriter, QuadReader, ConfigParameters}
import ldif.runtime.Quad
import ldif.util.{MemoryUsage, Consts, Uri}
import java.util.{ArrayList, List, HashSet => JHashSet}

class EntityBuilder (entityDescriptions : IndexedSeq[EntityDescription], readers : Seq[QuadReader], config: ConfigParameters) extends FactumBuilder with EntityBuilderTrait {

  private val log = LoggerFactory.getLogger(getClass.getName)
  // If this is true, quads like provenance quads (or even all quads) are saved for later use (merge)
  private val outputAllQuads = config.configProperties.getProperty("output", "mapped-only").toLowerCase=="all"
  private val saveQuads = config.otherQuadsWriter!=null
  private val saveSameAsQuads = config.sameAsWriter!=null
  private val provenanceGraph = config.configProperties.getProperty("provenanceGraph", "http://www4.wiwiss.fu-berlin.de/ldif/provenance")
  private val useExternalSameAsLinks = config.configProperties.getProperty("useExternalSameAsLinks", "true").toLowerCase=="true"
  private val ignoreProvenance = config.configProperties.getProperty("outputFormat", "nq").toLowerCase=="nt"

  // Property HT - Describes all the properties used in the Entity Description
  var PHT: PropertyHashTable = null
  // Forward HT - Contains connections which are going to be explored straight/forward
  var FHT:HashTable = new MemHashTable
  // Backward HT - Contains connections from quads which are going to be explored reverse/backward
  var BHT:HashTable = new MemHashTable

  // if no restriction is defined, build an entity for each resource
  var allUriNodes : Set[Node] = null
//  log.info("Memory used (before loading into hash tables): " + MemoryUsage.getMemoryUsage() +" KB")   //TODO: remove
  init
//  log.info("Memory used (after loaded into hash tables): " + MemoryUsage.getMemoryUsage() +" KB")  //TODO: remove

  // Build entities and write those into the EntityWriter
  def buildEntities (ed : EntityDescription, writer : EntityWriter) {
    val startTime = now
//    writer.entityDescription = ed
    val useAllUris = {
      ed.restriction.operator match {
        case None => true
        case _ => false
      }
    }

    if(!useAllUris){
      // entityNodes <- combination (as in the restriction pattern) of all the subjSets
      val entityNodes = getSubjSet(ed.restriction.operator) map (n => LocalNode.decompress(n))
      for (e <- entityNodes) {
          val entity = new EntityLocal(e, ed)
          writer.write(entity)
      }
    }
    else
      for(node <- allUriNodes)
        writer.write(new EntityLocal(LocalNode.decompress(node), ed))
//    log.info("Memory used (after writing all entities): " + MemoryUsage.getMemoryUsage()+" KB")   //TODO: remove
    writer.finish

    log.debug("Build Entities took " + ((now - startTime)) + " ms")
  }

  // Build a factum table from a given resource uri and an entity description
  override def buildFactumTable (entityResource : Node, pattern : IndexedSeq[Path]) = {

    // build the result table
    val valuesTable = getFactums(entityResource, pattern)

    // build a FactumTable from the table of values
    new FactumTableLocal(for (values <- valuesTable) yield new FactumRowLocal(values))
  }

  // -- private methods  --

  // Init memory structures
  private def init {
    EntityLocalMetadata.factumBuilder = this
    PHT = new PropertyHashTable(entityDescriptions)
    if(PHT.areAllUriNodesNeeded)
      allUriNodes = new JHashSet[Node]
      buildHashTables
  }

  // Build the forward/backward hash tables
  private def buildHashTables {
    FHT.clear
    BHT.clear
    var counter = 0

    // Round robin over reader
    while (readers.foldLeft(false)((a, b) => a || b.hasNext)){
      for (reader <- readers.filter(_.hasNext)) {
        val quad = reader.read

        if(saveQuads)
          saveQuadsForLater(quad)

        if(useExternalSameAsLinks)
          saveIfSameAsQuad(quad)

        if(isRelevantQuad(quad))  {
          counter += 1
//          if(counter % 100000 == 0)
//            log.info("Memory usage for " + counter + " loaded quads: " + MemoryUsage.getMemoryUsage() + "KB")

          val prop = StringPool.getCanonicalVersion(quad.predicate)
          val subj = LocalNode.intern(quad.subject)
          val obj = LocalNode.intern(quad.value)
          addUriNode(subj)
          addUriNode(obj)

          val v = PHT.get(prop)

          if (v == Some(PropertyType.FORW) || v == Some(PropertyType.BOTH))  {
            FHT.put(Pair(subj, prop), obj)
          }
          if (v == Some(PropertyType.BACK) || v == Some(PropertyType.BOTH))  {
            BHT.put(Pair(obj, prop), subj)
          }
        }
      }
    }
    log.info("EntityBuilder: " + counter + " quads loaded into the entity builder")

    //log.info("Read in Quads took " + ((now - startTime)) + " ms")
    //log.info(" [ FHT ] \n > keySet = ("+FHT.keySet.size.toString+")")
    //log.info(" [ BHT ] \n > keySet = ("+BHT.keySet.size.toString+")\n   - " + BHT.keySet.map(a => Pair.unapply(a).get._2 + " "+Pair.unapply(a).get._1.value).mkString("\n   - "))
  }

  private def saveQuadsForLater(quad: Quad) {
    if(outputAllQuads || (isProvenanceQuad(quad) && (!ignoreProvenance)))
      config.otherQuadsWriter.write(quad)
  }

  private def isRelevantQuad(quad: Quad): Boolean = {
    val prop = new Uri(quad.predicate).toString
    if(PHT.contains(prop) && !isProvenanceQuad(quad))
      true
    else
      false
  }

  private def saveIfSameAsQuad(quad: Quad) {
    if(saveSameAsQuads && quad.predicate=="http://www.w3.org/2002/07/owl#sameAs")
      config.sameAsWriter.write(quad)
  }

  private def isProvenanceQuad(quad: Quad): Boolean = {
    if(quad.graph==provenanceGraph)
      true
    else
      false
  }

  // Gathers all instances from the (relevant) input quads
  private def addUriNode(node: Node) {
    if(allUriNodes!=null && node.isUriNode)
        allUriNodes += node
  }

  // Build the subject set from a given operator
  private def getSubjSet(operator : Option[Operator]) : Set[Node] = {
     operator match {
          case Some(x:Condition) => getSubjSet(x)
          case Some(x:And) => getSubjSet(x)
          case Some(x:Or) => getSubjSet(x)
          case Some(x:Not) => new JHashSet[Node]  //TODO support Not operator - after M1
          case Some(x:Exists) => new JHashSet[Node]  //TODO support Exists operator - after M1
          case None => allUriNodes
        }
  }

  private def getSubjSet(and : And) : Set[Node] = {
    var subjSet : Set[Node] = new JHashSet[Node]
    for ((child,i) <- and.children.toSeq.zipWithIndex){
      val tmpSubjSet = getSubjSet(Some(child))
      if (i>0)
          subjSet = subjSet & tmpSubjSet
      else subjSet = tmpSubjSet
    }
    subjSet
  }

  private def getSubjSet(or : Or) : Set[Node] = {
    var subjSet : Set[Node] = new JHashSet[Node]
    for ((child,i) <- or.children.toSeq.zipWithIndex){
      val tmpSubjSet = getSubjSet(Some(child))
      subjSet = subjSet | tmpSubjSet
    }
    subjSet
  }

  private def getSubjSet(cond : Condition) : Set[Node]  = {
    val tmpSet = new Array[Set[Node]](cond.path.operators.size)
    for ((op,i) <- cond.path.operators.toSeq.reverse.zipWithIndex){
      if (i==0) {
        tmpSet(i) = evaluateOperator(op,cond.values)
      }
      else {
        tmpSet(i) = evaluateOperator(op,tmpSet(i-1),false)
      }
    }

    tmpSet(cond.path.operators.size-1)
  }

  /**
   * Evaluate a path operator
   * @param op : path operator to evaluate
   * @param srcNodes : set of nodes for which the operator has to be evaluated
   * @param direction : if false, reverse evaluation (from obj to subj)
   */
  private def evaluateOperator(op : PathOperator, srcNodes : Set[Node], direction : Boolean) = {
    var nodes = new JHashSet[Node]

    op match {
      case bo:BackwardOperator => {

        val prop = StringPool.getCanonicalVersion(bo.property.toString)

        for (srcNode <- srcNodes) {
          if (direction)
            BHT.get((srcNode, prop)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
          else
            FHT.get((srcNode, prop)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
        }
      }

      case fo:ForwardOperator =>  {
        val prop = StringPool.getCanonicalVersion(fo.property.toString)
        for (srcNode <- srcNodes)  {
          if (direction)
            FHT.get((srcNode, prop)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
          else
            BHT.get((srcNode, prop)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
        }
      }
      case pf:PropertyFilter =>  //TODO support PropertyFilter - after M1
      case lf:LanguageFilter =>  //TODO support LanguageFilter - after M1
    }
    nodes
  }

  // Helper method: analyse the operator for an immutable set of nodes
  private def evaluateOperator(op : PathOperator, values : collection.immutable.Set[NodeTrait]) : Set[Node] =   {
    val srcNodes = HashSet(values.toArray:_*) map (n => LocalNode.intern(n))  //from immutable to mutable
    evaluateOperator(op, srcNodes, false)
  }

  // Build the result table, given the seed/entity Uri and a sequence of paths
  private def getFactums(entityResource: Node, paths : IndexedSeq[Path]) : Traversable[IndexedSeq[Node]]  = {
    // init structures
    val prev = new ArraySeq[ArrayBuffer[Node]](paths.size)
    val next = new ArraySeq[ArrayBuffer[Traversable[Node]]](paths.size)
    val treeStructure = getTreeStructure(paths)
    val entityNode = LocalNode.intern(entityResource)
    val initRow = for (j <- 0 to paths.size-1) yield {
      prev(j) = ArrayBuffer(entityNode)
      next(j) = new ArrayBuffer[Traversable[Node]]
      entityNode
    }
    var valuesTable:Traversable[IndexedSeq[Node]] = Set(initRow)

    for (i <- 0 to maxPathSize(paths)-1){
      // for each level
      for ((path,j) <- paths.zipWithIndex.filter(_._1.operators.size > i)){
        next(j).clear
        for ((src, s) <- prev(j).zipWithIndex)  {
          // for each resource in the current path frontier
          next(j) += evaluateOperator(path.operators(i),Set(prev(j)(s)),true)
        }

        val k = getEqualPath(treeStructure,j,i)
        if (k>0)
          valuesTable = mergeCopy(valuesTable,k-1,j)
        else
          valuesTable = merge(valuesTable,prev(j),next(j),j)
        prev(j).clear
        for(nodes <- next(j))
          prev(j) ++= nodes
     }
    }
    for (row <- valuesTable) yield row map (n => LocalNode.decompress(n))
  }

  // Update column 'pathIndex' replacing prev level values with new ones
  private def merge(table : Traversable[IndexedSeq[Node]], prev:IndexedSeq[Node], next:IndexedSeq[Traversable[Node]], pathIndex : Int) = {
    var newTable = new JHashSet[ArraySeq[Node]]
    for (i <- 0 to prev.size-1)
      for ((row,j) <- table.toSeq.zipWithIndex.filter(_._1(pathIndex)==prev(i))){
        val newRows = for (newValue <- next(i)) yield {
          val newRow = ArraySeq(row:_*)  //from immutable to mutable
          newRow(pathIndex) = newValue
          newRow
        }
        newTable ++= newRows
      }
    newTable
  }

  // Update column 'to' as a copy of column 'from', since relative paths have a common sub-path
  private def mergeCopy(table : Traversable[IndexedSeq[Node]], from : Int, to : Int) = {
    var newTable = new JHashSet[ArraySeq[Node]]
    for (row <- table){
      val newRow = ArraySeq(row:_*)  //from immutable to mutable
      newRow(to) = newRow(from)
      newTable += newRow
    }
    newTable
  }

  // Return the index of a path which has the same sub-path (for a given 'level) of a given path ('pathIndex')
  private def getEqualPath(treeStructure : Array[Array[Int]], pathIndex : Int, level : Int) : Int = {
    var res = 0
    if (treeStructure != null){
      for (k <- 0 to pathIndex)
        if (treeStructure(k)(pathIndex)>level)
           res = k+1
    }
    res
  }

  // Return a table which describe the tree structure of the pattern
  private def getTreeStructure(paths : IndexedSeq[Path]) : Array[Array[Int]] = {
      val treeStructure = new Array[Array[Int]](paths.size,paths.size)
      var any = true
      for((aa, a) <- paths.zipWithIndex; (bb, b) <- paths.zipWithIndex)
          if (aa != bb)
            for (i <- 0 to min(aa.operators.size, bb.operators.size)-1) {
              if (aa.operators(i).toString == bb.operators(i).toString)
                  if (treeStructure(a)(b) == i){
                    treeStructure(a)(b) = i+1
                    any = false
                  }
            }
      if (any) null
      else treeStructure
  }

  // Return paths max length
  private def maxPathSize (paths : IndexedSeq[Path]) = {
    var max = 0
    for (size <- paths.map(x => x.operators.size))
      if (size > max)
        max = size
    max
  }

  private def forkAll(futures: Seq[Future[Any]]) {
    Futures.awaitAll(Consts.MAX_WAITING_TIME, futures: _*)
  }

  private def min(a:Int , b:Int) =  if (a<b) a else b

  private def now = System.currentTimeMillis
}

class PropertyHashTable(entityDescriptions: Seq[EntityDescription]) {
  private val hashtable = new HashMap[String, PropertyType.Value]
  private val log = LoggerFactory.getLogger(getClass.getName)
  private var allUriNodesNeeded = false

  buildPHT

  def areAllUriNodesNeeded = allUriNodesNeeded

  def contains(property: String) = hashtable.contains(property)

  def get(property: String) = hashtable.get(property)

  private def buildPHT {
     hashtable.clear
     val startTime = System.currentTimeMillis

     for (ed <- entityDescriptions){

       // analyse restriction
       addRestrictionProperties(ed.restriction.operator)

       // analyse patterns
       for (patterns <- ed.patterns)
         for (pattern <- patterns)
           for (op <- pattern.operators)
            op match {
               case op:ForwardOperator => updatePHT(op.property.toString, PropertyType.FORW)
               case op:BackwardOperator => updatePHT(op.property.toString, PropertyType.BACK)
               case _ =>
            }
     }

     log.info("Analyse Entity Descriptions took " + ((System.currentTimeMillis - startTime)) + " ms")
     //log.info(" [ PHT ] \n > keySet = ("+PHT.size.toString +") \n   - "+ PHT.mkString("\n   - "))
  }

    private def updatePHT(property :String, propertyType : PropertyType.Value ){
      hashtable.get(property) match {
        case Some(PropertyType.BOTH) =>
        case Some(PropertyType.FORW) =>
          if (propertyType==PropertyType.BACK)
            hashtable.put(property, PropertyType.BOTH)
          else hashtable.put(property, propertyType)
        case Some(PropertyType.BACK) =>
          if (propertyType==PropertyType.FORW)
            hashtable.put(property, PropertyType.BOTH)
          else hashtable.put(property, propertyType)
        case None => hashtable.put(property, propertyType)
      }
  }

  // Find all properties from a given operator and add those to the property hash table
  private def addRestrictionProperties(operator : Option[Operator]) {
    operator match {
      case Some(cond:Condition) =>
        for (op <- cond.path.operators){
          op match {
            case op:ForwardOperator => updatePHT(op.property.toString, PropertyType.BACK)
            case op:BackwardOperator => updatePHT(op.property.toString, PropertyType.FORW)
            case _ =>
          }
        }
      case Some(and:And) =>  {
        for (child <- and.children)
          addRestrictionProperties(Some(child))
      }
      case Some(or:Or) =>  {
        for (child <- or.children)
          addRestrictionProperties(Some(child))
      }
      case Some(not:Not) =>
      case Some(exists:Exists) =>
      case None => {
        allUriNodesNeeded = true
      }
    }
  }
}

object PropertyType extends Enumeration {
  val FORW, BACK, BOTH = Value
}

