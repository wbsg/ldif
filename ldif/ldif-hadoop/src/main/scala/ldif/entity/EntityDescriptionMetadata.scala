/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 19:03
 * To change this template use File | Settings | File Templates.
 */

/**
 * @param propertyMap A Map from a property URI to a sequence of pairs of (path ID, phase number)
 */
case class EntityDescriptionMetadata(entityDescriptions: IndexedSeq[EntityDescription], pathMap: Map[Int, PathInfo], propertyMap: Map[String, Seq[PropertyInfo]], entityDescriptionToIDMap: Map[EntityDescription, Int], pathIdMap: Map[Int, Int]) {
  def maxPhase(): Int = {
    var max = 0
    for(property <- propertyMap.values; propertyInfo <- property) {
      if(propertyInfo.phase > max)
        max = propertyInfo.phase
    }
    return max
  }

  def pathLength(id: Int): Int = {
    pathMap(id).length
  }

  def getPathInfosForPattern(entityDescriptionId: Int, patternIndex: Int): IndexedSeq[PathInfo] = {
    val pattern = entityDescriptions(entityDescriptionId).patterns(patternIndex)
    for(path <- pattern)
      yield pathMap.get(pathIdMap.get(path.gid).get).get
  }
}