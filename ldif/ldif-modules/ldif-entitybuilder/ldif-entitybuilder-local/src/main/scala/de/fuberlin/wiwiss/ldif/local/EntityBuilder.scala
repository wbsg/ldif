package de.fuberlin.wiwiss.ldif.local

import ldif.entity._
import java.util.logging.Logger
import ldif.util.Uri
import ldif.local.runtime.{EntityWriter, QuadReader}
import ldif.entity.Restriction._
import collection.mutable.{MultiMap, HashMap, Set, HashSet}

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

    //if (PHT.size!=0 && (FHT.size!=0 || BHW.size!=0)

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

    var valuesTable = getFactums(entityUri, pattern)

    // build a FactumTable from the table of values
    new FactumTableLocal(for (values <- valuesTable) yield new FactumRowLocal(values))
  }

  // -- private methods  --
 
  private def init {
     buildPHT
     buildHTs
  }

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
     log.info(" [ PHT ] \n > keySet = ("+PHT.size.toString +") \n   - "+ PHT.mkString("\n   - "))
  }

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
     log.info(" [ FHT ] \n > keySet = ("+FHT.keySet.size.toString+")")
     log.info(" [ BHT ] \n > keySet = ("+BHT.keySet.size.toString+")\n   - " + BHT.keySet.map(a => Pair.unapply(a).get._2 + " "+Pair.unapply(a).get._1.value).mkString("\n   - "))
  }


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

  // Build the subject set from a given condition
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

  private def getFactums(entityUri : String, paths : Seq[Path]) : Traversable[IndexedSeq[Node]] =
    getFactums(Set(Node.createUriNode(entityUri,null)),paths)

  private def getFactums(srcNodes : Set[Node], path : Path) : Traversable[IndexedSeq[Node]] = {
    var frontier = srcNodes
    for ((op,i) <- path.operators.toSeq.zipWithIndex) {
      frontier = evaluateOperator(op,frontier,true)
    }
    frontier.map(x => IndexedSeq(x))
  }

  private def getFactums(srcNodes : Set[Node], paths : Seq[Path]) : Traversable[IndexedSeq[Node]] = {
    var valuesTable:Traversable[IndexedSeq[Node]] = null
    for(path <- paths)
        valuesTable = combine(valuesTable,getFactums(srcNodes, path))
    valuesTable
  }

  // Build cartesian product
  private def combine(A:Traversable[IndexedSeq[Node]], B:Traversable[IndexedSeq[Node]]) = {
   if (A == null) B
   else if (B == null) A
   else for (a <- A; b <- B) yield a ++ b
  }

  /**
   * Evaluate a path operator
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
      case lf:PropertyFilter =>  //TODO support PropertFilter - after M1
      case lf:LanguageFilter =>  //TODO support LanguageFilter - after M1
    }
    nodes
  }

  // helper function
  // - analyse last operator + values of the condition
  private def evaluateOperator(op : PathOperator, values : collection.immutable.Set[String]) : Set[Node] =   {
    //TODO values could be nodes (instead of String)
    val srcNodes = new HashSet[Node]
    values.foreach(x => srcNodes += Node.fromString(x,null))
    evaluateOperator(op, srcNodes, false)
  }

  private def now = System.currentTimeMillis
}