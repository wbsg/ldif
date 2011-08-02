package de.fuberlin.wiwiss.ldif.mapreduce

import java.util.concurrent.atomic.AtomicInteger
import ldif.entity.{Path, Restriction, EntityDescription}
import ldif.entity.Restriction._
import scala.collection.mutable.{ArrayBuffer, HashMap, Seq => MSeq}
import ldif.entity.{BackwardOperator, ForwardOperator}


class EntityDescriptionMetaDataExtractor {
  var pathCounter = new AtomicInteger(0)
  // propertyMap stores information of (pathID, phaseNr)
  var propertyMap = new HashMap[String, MSeq[Pair[Int, Int]]]()
  var pathMap = new HashMap[Int, PathInfo]()

  def extract(entityDescriptions: Seq[EntityDescription]): EntityDescriptionMetadata = {
    pathCounter = new AtomicInteger(0)
    propertyMap = new HashMap[String, MSeq[Pair[Int, Int]]]()
    pathMap = new HashMap[Int, PathInfo]()

    for((entityDescription, index) <- entityDescriptions zipWithIndex) {
      extractEntityDescriptionMetaData(entityDescription, index)
    }

    EntityDescriptionMetadata(entityDescriptions, pathMap.clone.toMap, propertyMap.clone.toMap)
  }

  private def extractEntityDescriptionMetaData(entityDescription: EntityDescription, entityDescriptionIndex : Int) {
    extractPathInfoFromRestriction(entityDescription.restriction, entityDescriptionIndex)
    for((pattern, index) <- entityDescription.patterns zipWithIndex) {
      extractPathInfoFromPattern(pattern, entityDescriptionIndex, index)
    }
  }

  private def extractPathInfoFromPattern(pattern: IndexedSeq[Path], entityDescriptionIndex: Int, patternIndex: Int) {
    for((path, index) <- pattern zipWithIndex)
      extractPathInfo(path, entityDescriptionIndex, patternIndex, index, false)
  }

  private def extractPathInfoFromRestriction(restriction: Restriction, entityDescriptionIndex: Int) {
     restriction.operator match {
       case None => return
       case Some(operator) => extractPathInfoFromOperator(operator, entityDescriptionIndex)
     }
  }

  private def extractPathInfoFromOperator(operator: Operator, entityDescriptionIndex: Int) {
    operator match {
      case Condition(path, _) => extractPathInfo(path, entityDescriptionIndex, -1, -1, true)
      case Not(operator) => extractPathInfoFromOperator(operator, entityDescriptionIndex)
      case And(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescriptionIndex))
      case Or(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescriptionIndex))
      case Exists(path) => extractPathInfo(path, entityDescriptionIndex, -1, -1, true)
    }
  }

  private def extractPathInfo(path: Path, entityDescriptionIndex: Int, patternIndex: Int, pathIndex: Int, isRestrictionPath: Boolean) {
    val pathID = pathCounter.getAndIncrement
    pathMap.put(pathID, PathInfo(entityDescriptionIndex, patternIndex, pathIndex, path, isRestrictionPath))
    extractPropertyInfo(path,pathID)
  }

  private def extractPropertyInfo(path: Path, pathId: Int) {
    for((op,i) <- path.operators zipWithIndex) {
            op match {
               case op:ForwardOperator => addPropertyInfo(op.property.toString, pathId, i, true)
               case op:BackwardOperator => addPropertyInfo(op.property.toString, pathId, i, false)
               case _ =>    // TODO support filters
            }
      }
  }

  private def addPropertyInfo(property: String, pathId: Int, phase: Int, isForward : Boolean) {
    // TODO do we really need the phase here?

    val propertyInfoList = propertyMap.getOrElseUpdate(property, new ArrayBuffer[Pair[Int,Int]])
//    propertyInfoList + (pathId, phase)
  }

}