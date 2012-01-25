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
import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.ScoringFunction

/**
 * Scoring function that assigns real-valued, uniformly distributed scores to a list of graphs.
 * Used to model priority and can be applied to express, for example, reputation.
 *
 * Current implementation uses List.indexOf for lookups. Could be optimized.
 *
 * @author pablomendes
 */

class ScoredList(val priorityList: List[String]) extends ScoringFunction {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def getPosition(graphId: String) : Int = {
    priorityList.indexOf(graphId)
  }

  /**
   * Providing as input a list of nodes in an entity description, compute
   */
  def score(graphIds: Traversable[IndexedSeq[NodeTrait]]): Double = {
    graphIds.headOption match {
      case Some(g) => {
        val graphId = g(0).value // get value for first property path
        val position = getPosition(graphId)
        if (position>=0)
          (1 - ((position+1).toDouble / priorityList.size)) + 0.001 // last bit to distinguish between last item and out of list
        else
          0.0
      }
      case None => 0.0
    }
  }

  /**
   * Builds an object of type ScoringFunction based on an XML description
   */
  def fromXML(node: scala.xml.Node) : ScoringFunction = {
    null //todo implement
  }

}






