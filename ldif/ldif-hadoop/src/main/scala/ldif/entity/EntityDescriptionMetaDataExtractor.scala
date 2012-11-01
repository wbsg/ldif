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

package ldif.entity

import java.util.concurrent.atomic.AtomicInteger
import ldif.entity.Restriction._
import scala.collection.mutable.{ArrayBuffer, HashMap, Seq => MSeq}


object EntityDescriptionMetaDataExtractor {
  var pathCounter = new AtomicInteger(0)
  // propertyMap stores information of (pathID, phaseNr)
  var propertyMap = new HashMap[String, ArrayBuffer[PropertyInfo]]()
  var pathMap = new HashMap[Int, PathInfo]()
  var pathIdMap = new HashMap[Int, Int]()

  def extract(entityDescriptions: Seq[EntityDescription]): EntityDescriptionMetadata = {
    pathCounter = new AtomicInteger(0)
    propertyMap = new HashMap[String, ArrayBuffer[PropertyInfo]]()
    pathMap = new HashMap[Int, PathInfo]()
    pathIdMap = new HashMap[Int, Int]()

    for((entityDescription, index) <- entityDescriptions zipWithIndex) {
      extractEntityDescriptionMetaData(entityDescription, index)
    }

    val entityDescriptionMap = for((ed, index) <- entityDescriptions.zipWithIndex) yield (ed, index)
    EntityDescriptionMetadata(entityDescriptions.toIndexedSeq, pathMap.clone.toMap, propertyMap.clone.toMap, entityDescriptionMap.toMap, pathIdMap.toMap)
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
      case Condition(path, nodes) => extractPathInfo(path, entityDescriptionIndex, -1, -1, true, Some(nodes))
      case Not(operator) => extractPathInfoFromOperator(operator, entityDescriptionIndex)
      case And(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescriptionIndex))
      case Or(operators) => operators.foreach(extractPathInfoFromOperator(_, entityDescriptionIndex))
      case Exists(path) => extractPathInfo(path, entityDescriptionIndex, -1, -1, true)
    }
  }

  private def extractPathInfo(path: Path, entityDescriptionIndex: Int, patternIndex: Int, pathIndex: Int, isRestrictionPath: Boolean, restrictionValues: Option[Set[NodeTrait]] = None) {
    val pathID = pathCounter.getAndIncrement
    val properties = extractPropertyInfo(path,pathID, restrictionValues)
    pathMap.put(pathID, PathInfo(entityDescriptionIndex, patternIndex, pathIndex, path, isRestrictionPath, properties.length, properties))
    pathIdMap.put(path.gid, pathID)
  }

  // return a sequence of the properties in the path and for each property a flag if it is a forward path
  private def extractPropertyInfo(path: Path, pathId: Int, restrictionValues: Option[Set[NodeTrait]] = None): Seq[Pair[String, Boolean]] = {
    val properties = new ArrayBuffer[Pair[String, Boolean]]()
    var restrictionValuesToAdd: Option[Set[NodeTrait]] = None
    for((op,i) <- path.operators zipWithIndex) {
      if(restrictionValues!=None && i==path.operators.length-1)
        restrictionValuesToAdd = restrictionValues
      op match {
          case op:ForwardOperator => addPropertyInfo(op.property.toString, pathId, i, true, restrictionValuesToAdd)
            properties.append((op.property.toString, true))
          case op:BackwardOperator => addPropertyInfo(op.property.toString, pathId, i, false, restrictionValuesToAdd)
            properties.append((op.property.toString, false))
          case _ =>    // TODO support filters
        }
      }
    properties
  }

  private def addPropertyInfo(property: String, pathId: Int, phase: Int, isForward : Boolean, restrictionValues: Option[Set[NodeTrait]] = None) {
    val propertyInfoList: ArrayBuffer[PropertyInfo] = propertyMap.getOrElseUpdate(property, new ArrayBuffer[PropertyInfo])
    propertyInfoList.append(PropertyInfo(pathId, phase, isForward, restrictionValues))
  }

}

case class PropertyInfo(pathId: Int, phase: Int, isForward: Boolean, restrictionValues: Option[Set[NodeTrait]])