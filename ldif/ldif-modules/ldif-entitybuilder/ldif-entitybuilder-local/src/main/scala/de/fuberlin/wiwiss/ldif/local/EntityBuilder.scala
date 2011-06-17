package de.fuberlin.wiwiss.ldif.local

import ldif.entity._
import java.util.logging.Logger
import ldif.util.Uri
import ldif.entity.Restriction._
import collection.mutable.{ArraySeq, ArrayBuffer, HashMap, Set, HashSet}
import ldif.local.runtime.{LocalNode, EntityWriter, QuadReader}
import actors.{Future, Futures}
import ldif.local.util.Const
import java.util.ArrayList
import java.util.List
import ldif.local.runtime.Quad
import java.util.Collections
import ldif.local.runtime.{BackwardComparator, ForwardComparator}
import scala.collection.JavaConversions._

class EntityBuilder (entityDescriptions : IndexedSeq[EntityDescription], readers : Seq[QuadReader], useVoldemort: Boolean) extends FactumBuilder {
  private val nrOfQuadsPerSort = 500000
  private val log = Logger.getLogger(getClass.getName)

  object PropertyType extends Enumeration {
    val FORW, BACK, BOTH = Value
  }

  // Property HT - Describes all the properties used in the Entity Description
  val PHT = new HashMap[String, PropertyType.Value]
  // Forward HT - Contains connections which are going to be explored straight/forward
  var FHT:HashTable = new MemHashTable
  if(useVoldemort)
    FHT = new VoldermortHashTable("fht")
  // Backward HT - Contains connections from quads which are going to be explored reverse/backward
  var BHT:HashTable = new MemHashTable
  if(useVoldemort)
    BHT = new VoldermortHashTable("bht")

  // if no restriction is defined, build an entity for each resource
  var allUriNodes : Set[Node] = null

  init

  // Build entities and write those in the EntityWriter
  def buildEntities (ed : EntityDescription, writer : EntityWriter) {
    val startTime = now

    // entityNodes <- combination (as in the restriction pattern) of all the subjSets
    val entityNodes = getSubjSet(ed.restriction.operator)

    for (e <- entityNodes) {
      val entity = new EntityLocal(e.value, ed, this)
      writer.write(entity)
    }
    writer.finish

    log.info("Build Entities took " + ((now - startTime)) + " ms")
  }

  // Build a factum table from a given resource uri and an entity description
  override def buildFactumTable (entityUri : String, pattern : IndexedSeq[Path]) = {

    // build the result table
    val valuesTable = getFactums(entityUri, pattern)

    // build a FactumTable from the table of values
    new FactumTableLocal(for (values <- valuesTable) yield new FactumRowLocal(values))
  }

  // -- private methods  --

  // Init memory structures
  private def init {
    buildPHT
    if(useVoldemort)
      buildVoldemortHTs
    else
      buildHTs
  }

  // Build the property hash table
  private def buildPHT {
     PHT.clear
     val startTime = now

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

     log.info("Analyse Entity Descriptions took " + ((now - startTime)) + " ms")
     //log.info(" [ PHT ] \n > keySet = ("+PHT.size.toString +") \n   - "+ PHT.mkString("\n   - "))
  }

  // Build the forward/backward hash tables
  private def buildHTs {
    FHT.clear
    BHT.clear
    val startTime = now

    // Round robin over readers
    while (readers.foldLeft(false)((a, b) => a || b.hasNext)){
      for (reader <- readers.filter(_.hasNext)) {
        val quad = reader.read

        val prop = new Uri(quad.predicate).toString

        addUriNodes(quad)

        val v = PHT.get(prop)

        if (v == Some(PropertyType.FORW) || v == Some(PropertyType.BOTH))  {
          FHT.put(Pair(quad.subject, prop), quad.value)
        }
        if (v == Some(PropertyType.BACK) || v == Some(PropertyType.BOTH))  {
          BHT.put(Pair(quad.value, prop), quad.subject)
        }
      }
    }

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

  private def isProvenanceQuad(quad: Quad): Boolean = {
    if(quad.graph=="http://www4.wiwiss.fu-berlin.de/ldif/provenance")//TODO: Use this graph?
      true
    else
      false
  }

  // Build HTs in a Voldemort specific way. This is done because the in-memory hashtable strategy is not applicable here.
  // The Sets to store values had to be deserialized and serialized every time a value was added.
  private def buildVoldemortHTs {
    FHT.clear
    BHT.clear
    val startTime = now

     //Voldemort specific
       for (reader <- readers) {
           var completeCount = 0
           var count = 0
           val quadList: List[Quad] = new ArrayList[Quad]

         val watch = new StopWatch
         watch.getTimeSpanInSeconds
           while (reader.hasNext){
             val quad = reader.read
             quadList.add(quad)
             addUriNodes(quad)
             count += 1
             completeCount += 1
             if(count==nrOfQuadsPerSort) {
               addQuadsToFHT(quadList)
               addQuadsToBHT(quadList)
               count = 0
               System.out.println("Number of Quads written to Voldemort: " + completeCount + " in " + watch.getTimeSpanInSeconds + "s")
             }
           }
           // Write remaining quads to table
           if(count>0) {
              addQuadsToFHT(quadList)
              addQuadsToBHT(quadList)
              count = 0
           }
           // Write remaining values to Voldemort
           BHT.asInstanceOf[VoldermortHashTable].finishPut()
           FHT.asInstanceOf[VoldermortHashTable].finishPut()
        }
  }

  private def addUriNodes(quad:Quad) {
    if(allUriNodes!=null){
      if (quad.subject.isUriNode)
        allUriNodes += quad.subject
      if (quad.value.isUriNode)
        allUriNodes += quad.value
    }
  }

  private def addQuadsToFHT(quads: List[Quad]) {
    Collections.sort(quads, new ForwardComparator)
    var lastSubject: Node = null
    var lastPredicate: String = null

    for(quad <- quads) {
      val property = new Uri(quad.predicate).toString
      val propertyType = PHT.get(property)
      if (propertyType == Some(PropertyType.FORW) || propertyType == Some(PropertyType.BOTH))
        FHT.put(Pair(quad.subject, property), quad.value)
    }
  }

  private def addQuadsToBHT(quads: List[Quad]) {
    Collections.sort(quads, new BackwardComparator)
    var lastSubject: Node = null
    var lastPredicate: String = null

    for(quad <- quads) {
      val property = new Uri(quad.predicate).toString
      val propertyType = PHT.get(property)
      if (propertyType == Some(PropertyType.BACK) || propertyType == Some(PropertyType.BOTH))
        BHT.put(Pair(quad.value, property), quad.subject)
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
        allUriNodes = new HashSet[Node]
      }
    }
  }

  private def updatePHT(property :String, propertyType : PropertyType.Value ){
    PHT.get(property) match {
      case Some(PropertyType.BOTH) =>
      case Some(PropertyType.FORW) =>
        if (propertyType==PropertyType.BACK)
          PHT.put(property, PropertyType.BOTH)
        else PHT.put(property, propertyType)
      case Some(PropertyType.BACK) =>
        if (propertyType==PropertyType.FORW)
          PHT.put(property, PropertyType.BOTH)
        else PHT.put(property, propertyType)
      case None => PHT.put(property, propertyType)
    }

  }

  // Build the subject set from a given operator
  private def getSubjSet(operator : Option[Operator]) : Set[Node] = {
     operator match {
          case Some(x:Condition) => getSubjSet(x)
          case Some(x:And) => getSubjSet(x)
          case Some(x:Or) => getSubjSet(x)
          case Some(x:Not) => new HashSet[Node]  //TODO support Not operator - after M1
          case Some(x:Exists) => new HashSet[Node]  //TODO support Exists operator - after M1
          case None => allUriNodes
        }
  }

  private def getSubjSet(and : And) : Set[Node] = {
    var subjSet : Set[Node] = new HashSet[Node]
    for ((child,i) <- and.children.toSeq.zipWithIndex){
      val tmpSubjSet = getSubjSet(Some(child))
      if (i>0)
          subjSet = subjSet & tmpSubjSet
      else subjSet = tmpSubjSet
    }
    subjSet
  }

  private def getSubjSet(or : Or) : Set[Node] = {
    var subjSet : Set[Node] = new HashSet[Node]
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
    // filter out blank nodes
    tmpSet(cond.path.operators.size-1).filter(_.isUriNode)
  }

  /**
   * Evaluate a path operator
   * @param op : path operator to evaluate
   * @param srcNodes : set of nodes for which the operator has to be evaluated
   * @param direction : if false, reverse evaluation (from obj to subj)
   */
  private def evaluateOperator(op : PathOperator, srcNodes : Set[Node], direction : Boolean) = {
    var nodes = new HashSet[Node]

    op match {
      case bo:BackwardOperator =>
        for (srcNode <- srcNodes) {
          if (direction)
            BHT.get((srcNode, bo.property.toString)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
          else
            FHT.get((srcNode, bo.property.toString)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
        }

      case fo:ForwardOperator =>
        for (srcNode <- srcNodes)  {
          if (direction)
            FHT.get((srcNode, fo.property.toString)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
          else
            BHT.get((srcNode, fo.property.toString)) match {
              case Some(node) => nodes ++= node
              case None =>
            }
        }
      case pf:PropertyFilter =>  //TODO support PropertyFilter - after M1
      case lf:LanguageFilter =>  //TODO support LanguageFilter - after M1
    }
    nodes
  }

  // Helper method: analyse the operator for an immutable set of nodes
  private def evaluateOperator(op : PathOperator, values : collection.immutable.Set[Node]) : Set[Node] =   {
    val srcNodes = HashSet(values.toArray:_*)  //from immutable to mutable
    evaluateOperator(op, srcNodes, false)
  }

  // Build the result table, given the seed/entity Uri and a sequence of paths
  private def getFactums(entityUri : String, paths : IndexedSeq[Path]) : Traversable[IndexedSeq[Node]]  = {
    // init structures
    val prev = new ArraySeq[ArrayBuffer[Node]](paths.size)
    val next = new ArraySeq[ArrayBuffer[Traversable[Node]]](paths.size)
    val treeStructure = getTreeStructure(paths)
    val entityNode = LocalNode.createUriNode(entityUri,null)
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
    valuesTable
  }

  // Update column 'pathIndex' replacing prev level values with new ones
  private def merge(table : Traversable[IndexedSeq[Node]], prev:IndexedSeq[Node], next:IndexedSeq[Traversable[Node]], pathIndex : Int) = {
    var newTable = new HashSet[ArraySeq[Node]]
    for (i <- 0 to prev.size-1)
      for ((row,j) <- table.toSeq.zipWithIndex.filter(_._1(pathIndex) == prev(i))){
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
    var newTable = new HashSet[ArraySeq[Node]]
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
    Futures.awaitAll(Const.MAX_WAITING_TIME, futures: _*)
  }

  private def min(a:Int , b:Int) =  if (a<b) a else b

  private def now = System.currentTimeMillis
}

class StopWatch {
  private var lastTime = System.currentTimeMillis

  def getTimeSpanInSeconds(): Double = {
    val newTime = System.currentTimeMillis
    val span = newTime - lastTime
    lastTime = newTime
    span / 1000.0
  }
}
