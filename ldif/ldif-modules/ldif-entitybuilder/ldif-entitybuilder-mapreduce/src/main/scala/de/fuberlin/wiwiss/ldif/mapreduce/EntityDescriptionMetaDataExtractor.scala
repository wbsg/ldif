package de.fuberlin.wiwiss.ldif.mapreduce

import java.util.concurrent.atomic.AtomicInteger
import ldif.entity.{Path, Restriction, EntityDescription}
import ldif.entity.Restriction._
import scala.collection.mutable.{ArrayBuffer, HashMap, Seq => MSeq}
import ldif.entity.{BackwardOperator, ForwardOperator, PropertyFilter, LanguageFilter}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */

class EntityDescriptionMetaDataExtractor {
  var pathCounter = new AtomicInteger(0)
  // propertyMap stores information of (pathID, phaseNr)
  var propertyMap = new HashMap[String, MSeq[Pair[Int, Int]]]()
  var pathMap = new HashMap[Int, PathInfo]()

  def extract(entityDescriptions: Seq[EntityDescription]): EntityDescriptionMetadata = {
    pathCounter = new AtomicInteger(0)
    propertyMap = new HashMap[String, MSeq[Pair[Int, Int]]]()
    pathMap = new HashMap[Int, PathInfo]()

    for(entityDescription <- entityDescriptions) {
      extractEntityDesriptionMetaData(entityDescription)
    }

    EntityDescriptionMetadata(entityDescriptions, pathMap.clone.toMap, propertyMap.clone.toMap)
  }

  private def extractEntityDesriptionMetaData(entityDescription: EntityDescription) {
    extractPathInfoFromRestriction(entityDescription.restriction, entityDescription)
    for((pattern, index) <- entityDescription.patterns zip entityDescription.patterns.indices) {
      extractPathInfoFromPattern(entityDescription, pattern, index)
    }
  }

  private def extractPathInfoFromPattern(entityDescription: EntityDescription, pattern: IndexedSeq[Path], patternIndex: Int) {
    for((path, index) <- pattern zip pattern.indices)
      extractPathInfo(path, entityDescription, patternIndex, index, false)
  }

  private def extractPathInfoFromRestriction(restriction: Restriction, entityDescription: EntityDescription) {
     restriction.operator match {
       case None => return
       case Some(operator) => extractPathInfoFromOperator(operator, entityDescription)
     }
  }

  private def extractPathInfoFromOperator(operator: Operator, entityDescription: EntityDescription) {
    operator match {
      case Condition(path, _) => extractPathInfo(path, entityDescription, -1, -1, true)
      case Not(operator) => extractPathInfoFromOperator(operator, entityDescription)
      case And(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescription))
      case Or(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescription))
      case Exists(path) => extractPathInfo(path, entityDescription, -1, -1, true)
    }
  }

  private def extractPathInfo(path: Path, entityDescription: EntityDescription, patternIndex: Int, pathIndex: Int, isRestrictionPath: Boolean) {
    val pathID = pathCounter.getAndIncrement
    pathMap.put(pathID, PathInfo(entityDescription, patternIndex, pathIndex, path, isRestrictionPath))

  }

  private def extractPropertyInfo(path: Path, pathId: Int) {
    val phaseNr = 0
    for(op <- path.operators) {
      op match {
        case _ =>
      }
    }
  }

  private def addPropertyInfo(property: String, pathId: Int, phase: Int) {
    val propertyInfoList = propertyMap.getOrElseUpdate(property, new ArrayBuffer[Pair[Int,Int]])
//    propertyInfoList + (pathId, phase)
  }
}