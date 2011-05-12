package de.fuberlin.wiwiss.ldif.local

import ldif.entity._
import java.util.logging.Logger
import ldif.util.Uri
import ldif.local.runtime.{EntityWriter, QuadReader}
import ldif.entity.Restriction._
import collection.mutable.{ArraySeq, ArrayBuffer, MultiMap, HashMap, Set, HashSet}
//import collection.mutable.{ArraySeq, ArrayBuffer, MultiMap, HashMap, Set, HashSet}

class EntityBuilder (entityDescriptions : IndexedSeq[EntityDescription], reader : QuadReader) extends FactumBuilder {

  private val log = Logger.getLogger(getClass.getName)

  object PropertyType extends Enumeration {
    val FORW, BACK, BOTH = Value
  }

  // Property HT - Describes all the properties used in the Entity Description
  val PHT = new HashMap[String, PropertyType.Value]
  // Forward HT - Contains connections which are going to be explored straight/forward
  val FHT:MultiMap[Pair[Node,String], Node] = new HashMap[Pair[Node,String], Set[Node]] with MultiMap[Pair[Node,String], Node]
  // Backward HT - Contains connections from quads which are going to be explored reverse/backward
  val BHT:MultiMap[Pair[Node,String], Node] = new HashMap[Pair[Node,String], Set[Node]] with MultiMap[Pair[Node,String], Node]

  init

  // Build entities and write those in the EntityWriter
  def buildEntities (ed : EntityDescription, writer : EntityWriter) {
    log.info("\n--------------------------------\n Build Entities \n--------------------------------")
    val startTime = now

    // entityNodes <- combination (as in the restriction pattern) of all the subjSets
    val entityNodes = getSubjSet(ed.restriction.operator)

    for (e <- entityNodes) {
      val entity = new EntityLocal(e.value, ed, this)
      writer.write(entity)
    }
    log.info("Total time: " + ((now - startTime) / 1000.0) + " seconds")
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
     buildHTs
  }

  // Build the property hash table
  private def buildPHT {
     PHT.clear
     log.info("\n--------------------------------\n Analyse Entity Descriptions \n--------------------------------")
     val startTime = now

     for (ed <- entityDescriptions){
       //TODO if already in PHT, set value to BOTH (in case)

       // analyse restriction
       addRestrictionProperties(ed.restriction.operator)

       // analyse patterns
       for (patterns <- ed.patterns)
         for (pattern <- patterns)
           for (op <- pattern.operators)
            op match {
               case op:ForwardOperator => PHT.put(op.property.toString, PropertyType.FORW)
               case op:BackwardOperator => PHT.put(op.property.toString, PropertyType.BACK)
               case _ =>
            }
     }

     log.info("Total time: " + ((now - startTime) / 1000.0) + " seconds")
     //log.info(" [ PHT ] \n > keySet = ("+PHT.size.toString +") \n   - "+ PHT.mkString("\n   - "))
  }

  // Build the forward/backward hash tables
  private def buildHTs {
     FHT.clear
     BHT.clear
     log.info("\n--------------------------------\n Read in Quads \n--------------------------------")
     val startTime = now

     while (!reader.isEmpty){
       val quad = reader.read

       val prop = new Uri(quad.predicate).toString

       val v = PHT.get(prop)

       if (v == Some(PropertyType.FORW) || v == Some(PropertyType.BOTH))  {
          FHT.addBinding(Pair(Node.fromString(quad.subject,quad.graph), prop), Node.fromString(quad.value,quad.graph))
       }
       if (v == Some(PropertyType.BACK) || v == Some(PropertyType.BOTH))  {
          BHT.addBinding(Pair(Node.fromString(quad.value,quad.graph), prop), Node.fromString(quad.subject,quad.graph))
       }
     }

     log.info("Total time: " + ((now - startTime) / 1000.0) + " seconds")
     //log.info(" [ FHT ] \n > keySet = ("+FHT.keySet.size.toString+")")
     //log.info(" [ BHT ] \n > keySet = ("+BHT.keySet.size.toString+")\n   - " + BHT.keySet.map(a => Pair.unapply(a).get._2 + " "+Pair.unapply(a).get._1.value).mkString("\n   - "))
  }

  // Find all properties in a given operator and add those to the property hash table
  private def addRestrictionProperties(operator : Option[Operator]) {
        operator match {
          case Some(cond:Condition) =>
            for (op <- cond.path.operators){
              op match {
                case op:ForwardOperator => PHT.put(op.property.toString, PropertyType.BACK)
                case op:BackwardOperator => PHT.put(op.property.toString, PropertyType.FORW)
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
          case None =>
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
          case None => new HashSet[Node]
        }
  }

  private def getSubjSet(and : And) : Set[Node] = {
    var subjSet:Set[Node] = null
    for ((child,i) <- and.children.toSeq.zipWithIndex){
      val tmpSubjSet = getSubjSet(Some(child))
      if (i>0)
          subjSet = subjSet & tmpSubjSet
      else subjSet = tmpSubjSet
    }
    subjSet
  }

  private def getSubjSet(or : Or) : Set[Node] = {
    var subjSet:Set[Node] = null
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
    tmpSet(cond.path.operators.size-1).filter(x => x.nodeType!=Node.BlankNode )
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
      case pf:PropertyFilter =>  //TODO support PropertFilter - after M1
      case lf:LanguageFilter =>  //TODO support LanguageFilter - after M1
    }
    nodes
  }

  // Helper method: analyse the operator for a set of String values
  private def evaluateOperator(op : PathOperator, values : collection.immutable.Set[String]) : Set[Node] =   {
    //TODO values could be nodes (instead of String)
    val srcNodes = new HashSet[Node]
    values.foreach(x => srcNodes += Node.fromString(x,null))
    evaluateOperator(op, srcNodes, false)
  }

  // Build the result table, given the seed/entity Uri and a sequence of paths
  private def getFactums(entityUri : String, paths : IndexedSeq[Path]) : Traversable[IndexedSeq[Node]]  = {
    var valuesTable : Traversable[IndexedSeq[Node]] = null
    val prev = new ArraySeq[ArrayBuffer[Node]](paths.size)
    val next = new ArraySeq[ArrayBuffer[Traversable[Node]]](paths.size)
    val treeStructure = getTreeStructure(paths)

    // init structures
    val entityNode = Node.createUriNode(entityUri,null)
    val initRow = for (j <- 0 to paths.size-1) yield {
      prev(j) = ArrayBuffer(entityNode)
      next(j) = new ArrayBuffer[Traversable[Node]]
      entityNode  
    }
    valuesTable = Set(initRow)

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

  private def min(a:Int , b:Int) =  if (a<b) a else b
  
  private def now = System.currentTimeMillis
}