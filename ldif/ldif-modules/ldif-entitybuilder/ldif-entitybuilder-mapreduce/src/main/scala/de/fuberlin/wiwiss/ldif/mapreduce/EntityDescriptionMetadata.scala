package de.fuberlin.wiwiss.ldif.mapreduce

import ldif.entity.{Path, EntityDescription}

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
case class EntityDescriptionMetadata(entityDescriptions: IndexedSeq[EntityDescription], pathMap: Map[Int, PathInfo], propertyMap: Map[String, Seq[PropertyInfo]], entityDescriptionMap: Map[EntityDescription, Int]) {
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
}