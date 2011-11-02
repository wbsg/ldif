package ldif.entity

import java.util.concurrent.atomic.AtomicInteger
import ldif.entity.Restriction._
import scala.collection.mutable.{ArrayBuffer, HashMap, Seq => MSeq}


class EntityDescriptionMetaDataExtractor {
  var pathCounter = new AtomicInteger(0)
  // propertyMap stores information of (pathID, phaseNr)
  var propertyMap = new HashMap[String, ArrayBuffer[PropertyInfo]]()
  var pathMap = new HashMap[Int, PathInfo]()
  var pathIdMap = new HashMap[Path, Int]()

  def extract(entityDescriptions: Seq[EntityDescription]): EntityDescriptionMetadata = {
    pathCounter = new AtomicInteger(0)
    propertyMap = new HashMap[String, ArrayBuffer[PropertyInfo]]()
    pathMap = new HashMap[Int, PathInfo]()

    for((entityDescription, index) <- entityDescriptions zipWithIndex) {
      extractEntityDescriptionMetaData(entityDescription, index)
    }

    val entityDescriptionMap = for((ed, index) <- entityDescriptions.zipWithIndex) yield (ed, index)
    val idToEntityDescriptionMap = for((ed, index) <- entityDescriptions.zipWithIndex) yield (index, ed)
    EntityDescriptionMetadata(entityDescriptions.toIndexedSeq, pathMap.clone.toMap, propertyMap.clone.toMap, entityDescriptionMap.toMap, idToEntityDescriptionMap.toMap)
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
    val properties = extractPropertyInfo(path,pathID)
    pathMap.put(pathID, PathInfo(entityDescriptionIndex, patternIndex, pathIndex, path, isRestrictionPath, properties.length, properties))
    pathIdMap.put(path, pathID)
  }

  private def extractPropertyInfo(path: Path, pathId: Int): Seq[String] = {
    val properties = new ArrayBuffer[String]()
    for((op,i) <- path.operators zipWithIndex) {
      op match {
          case op:ForwardOperator => addPropertyInfo(op.property.toString, pathId, i, true)
            properties.append(op.property.toString)
          case op:BackwardOperator => addPropertyInfo(op.property.toString, pathId, i, false)
            properties.append(op.property.toString)
          case _ =>    // TODO support filters
        }
      }
    properties
  }

  private def addPropertyInfo(property: String, pathId: Int, phase: Int, isForward : Boolean) {
    val propertyInfoList: ArrayBuffer[PropertyInfo] = propertyMap.getOrElseUpdate(property, new ArrayBuffer[PropertyInfo])
    propertyInfoList.append(PropertyInfo(pathId, phase, isForward))
  }

}

case class PropertyInfo(pathId: Int, phase: Int, isForward: Boolean)