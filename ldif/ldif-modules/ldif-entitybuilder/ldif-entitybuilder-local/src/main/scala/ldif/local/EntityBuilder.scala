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

package ldif.local

import ldif.entity._
import org.slf4j.LoggerFactory
import ldif.entity.Restriction._
import collection.mutable.{ArraySeq, ArrayBuffer, Set, HashSet}
import actors.{Future, Futures}
import runtime.impl.{QuadQueue, MultiQuadReader}
import scala.collection.JavaConversions._
import ldif.local.runtime.{LocalNode, EntityWriter, QuadReader, ConfigParameters}
import ldif.runtime.Quad
import java.util.{HashSet => JHashSet}
import util.{EntityBuilderReportPublisher, StringPool}
import ldif.util.{JobMonitor, ReportPublisher, Consts, Uri}

class EntityBuilder (entityDescriptions : IndexedSeq[EntityDescription], readers : Seq[QuadReader], config: ConfigParameters) extends FactumBuilder with EntityBuilderTrait {
  private val log = LoggerFactory.getLogger(getClass.getName)
  // If this is true, quads like provenance quads (or even all quads) are saved for later use (merge)
  private val collectNotUsedQuads = config.collectNotUsedQuads

  // Property HT - Describes all the properties used in the Entity Description
  var PHT:PropertyHashTable = new PropertyHashTable(entityDescriptions)
  // Forward HT - Contains connections which are going to be explored straight/forward
  var FHT:HashTable =
    if (collectNotUsedQuads) {
     // new MarkedMemHashTable
     new MemHashTableReadOnce
    }
    else
      new MemHashTable
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
          entityBuilderReportPublisher.entitiesBuilt.incrementAndGet()
      }
    }
    else
      for(node <- allUriNodes)
        writer.write(new EntityLocal(LocalNode.decompress(node), ed))
//    log.info("Memory used (after writing all entities): " + MemoryUsage.getMemoryUsage()+" KB")   //TODO: remove
    writer.finish
    entityBuilderReportPublisher.entityQueuesFilled.incrementAndGet()

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
    entityBuilderReportPublisher.setStartTime
    EntityLocalMetadata.factumBuilder = this
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

        entityBuilderReportPublisher.quadsReadCounter.incrementAndGet()

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
    entityBuilderReportPublisher.finishedReading = true
    log.info("EntityBuilder: " + counter + " quads loaded into the entity builder")

    //log.info("Read in Quads took " + ((now - startTime)) + " ms")
    //log.info(" [ FHT ] \n > keySet = ("+FHT.keySet.size.toString+")")
    //log.info(" [ BHT ] \n > keySet = ("+BHT.keySet.size.toString+")\n   - " + BHT.keySet.map(a => Pair.unapply(a).get._2 + " "+Pair.unapply(a).get._1.value).mkString("\n   - "))
  }

  private def isRelevantQuad(quad: Quad): Boolean = {
    val prop = new Uri(quad.predicate).toString
    if(PHT.contains(prop))
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
          case Some(x:Not) => throw new UnsupportedOperationException("Restriction operator 'Not' is not implemented, yet")  //TODO support Not operator - after M1
          case Some(x:Exists) =>  getSubjSet(x)
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

  private def getSubjSet(exists : Exists) : Set[Node] = {
    val tmpSet = new Array[Set[Node]](exists.path.operators.size)
    for ((op,i) <- exists.path.operators.toSeq.reverse.zipWithIndex){
      if (i==0) {
        tmpSet(i) = evaluateOperator(op)
      }
      else {
        tmpSet(i) = evaluateOperator(op,tmpSet(i-1),false)
      }
    }
    tmpSet(exists.path.operators.size-1)
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
    val nodes = new JHashSet[Node]

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
      case pf:PropertyFilter =>  //TODO support PropertyFilter
      case lf:LanguageFilter =>  //TODO support LanguageFilter
    }
    nodes
  }

  // Helper method: analyse the operator for an immutable set of nodes
  private def evaluateOperator(op : PathOperator, values : collection.immutable.Set[NodeTrait]) : Set[Node] =   {
    val srcNodes = HashSet(values.toArray:_*) map (n => LocalNode.intern(n))  //from immutable to mutable
    evaluateOperator(op, srcNodes, false)
  }

  // Evaluate the last path operator for an Exists condition
  private def evaluateOperator(op : PathOperator) : Set[Node] =   {
    val nodes = new JHashSet[Node]

    op match {
      case bo:BackwardOperator => {
        val prop = StringPool.getCanonicalVersion(bo.property.toString)
        for (srcNode <- allUriNodes) {
            BHT.get((srcNode, prop)) match {
              case Some(node) => nodes += srcNode
              case None =>
            }
        }
      }

      case fo:ForwardOperator =>  {
        val prop = StringPool.getCanonicalVersion(fo.property.toString)
        for (srcNode <- allUriNodes)  {
            FHT.get((srcNode, prop)) match {
              case Some(node) => nodes += srcNode
              case None =>
            }
        }
      }
      case pf:PropertyFilter =>  //TODO support PropertyFilter
      case lf:LanguageFilter =>  //TODO support LanguageFilter
    }
    nodes
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

  private def appendToOtherQuads(reader : QuadReader) {
      while (reader.hasNext)
        config.otherQuadsWriter.write(reader.read())
  }

  // Retrieves quads stored in the internal HTs but not used for building entities
  // Based on the assumption that EDs use only
  //  - rdf:type restrictions
  //  - forward and length=1 paths as pattern
  override def getNotUsedQuads : QuadReader = {
    if (collectNotUsedQuads) {
      // retrieves not-used property quads
      //val f = FHT.asInstanceOf[MarkedMemHashTable].getNotUsedQuads(PropertyType.FORW)
      val f = FHT.asInstanceOf[MemHashTableReadOnce].getNotUsedQuads(PropertyType.FORW)
      // retrieves rdf:type quads
      val b = BHT.getAllQuads(PropertyType.BACK)
      new MultiQuadReader(f,b)
    }
    else
      new QuadQueue
  }
}
