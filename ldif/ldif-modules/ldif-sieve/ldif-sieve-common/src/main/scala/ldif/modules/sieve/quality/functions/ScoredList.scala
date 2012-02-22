package ldif.modules.sieve.quality.functions

/*
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

import org.slf4j.LoggerFactory
import ldif.modules.sieve.quality.ScoringFunction
import ldif.entity.{Entity, NodeTrait}

/**
 * Scoring function that assigns real-valued, uniformly distributed scores to a list of graphs.
 * Used to model priority and can be applied to express, for example, reputation.
 * Values are not comparable across different configuration files.
 *
 * Current implementation uses List.indexOf for lookups. Could be optimized.
 *
 * @author pablomendes
 */

class ScoredList(val priorityList: List[String]) extends ScoringFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

//  def getPosition(graphId: String) : Int = {
//    priorityList.indexOf(graphId)
//  }

  def getPosition(graphId: String) : Int = {
    priorityList.filter(graphPrefix => graphId startsWith graphPrefix).headOption match {
      case Some(g) => priorityList.indexOf(g)
      case None => -1
    }
  }

  override def toString() : String = {
    "ScoredList, priority=" + priorityList
  }

  /**
   * Compute a score for the provided graph given its position in the priority list.
   * metadataValues are not used here.
   */
  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
        val position = getPosition(graphId.value)
        if (position == 0) {
            1
        } else if (position>0) {
            (1 - ((position+1).toDouble / priorityList.size)) + 0.001 // last bit to distinguish between last item and out of list
        } else {
            0.0
        }
  }

}

object ScoredList {
  def fromXML(node: scala.xml.Node) : ScoringFunction = {
    val paramStr : String = (node \ "Param" \ "@value").text
    val params : List[String] = paramStr.split(" ").toList
    if (params.length < 1) {
      throw new IllegalArgumentException("No list of values given as preference")
    }
    new ScoredList(params)
  }
}







//  /**
//   * Providing as input a list of nodes in an entity description, compute
//   */
//  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
//    graphIds.headOption match {
//      case Some(g) => {
//        val graphId = g(0).value // get value for first property path
//        val position = getPosition(graphId)
//        if (position == 0) {
//            1
//        } else if (position>0) {
//            (1 - ((position+1).toDouble / priorityList.size)) + 0.001 // last bit to distinguish between last item and out of list
//        } else {
//            0.0
//        }
//      }
//      case None => 0.0
//    }
//  }
