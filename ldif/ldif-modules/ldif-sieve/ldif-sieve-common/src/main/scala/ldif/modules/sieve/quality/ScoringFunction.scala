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

package ldif.modules.sieve.quality

import functions.{Threshold, IntervalMembership, TimeCloseness, ScoredList}
import xml.Node
import ldif.entity.{Entity, NodeTrait}

/**
 * Constructor of implementing classes should accept applicable Param and EnvironmentVariable values.
 * The values described in Input are passed at scoring time to the method "score".
 * @author Pablo Mendes
 * @author Hannes Mühleisen
 */

trait ScoringFunction {
  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double
}

trait ScoringFunctionConjunctive extends ScoringFunction {
  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
    var res:Double = 1
    metadataValues.foreach((s) => s.foreach((n) => {
      res = math.min(scoreSingleValue(n),res)
    }))
    return res
  }
  def scoreSingleValue(node : NodeTrait) : Double
}

object ScoringFunction {
  def create(className: String, config: Node): ScoringFunction = className.toLowerCase match {
    case "scoredlist" => return ScoredList.fromXML(config)
    case "timecloseness" => return TimeCloseness.fromXML(config)
    case "interval" => return IntervalMembership.fromXML(config)
    case "threshold" => return Threshold.fromXML(config)

    // NOTICE: add case statements for new scoring functions here
    case whatever => throw new IllegalArgumentException("Unable to construct scoring function for class name " + className)
  }

  def getStringConfig(node: Node, key: String): String = {
    def filterAttribute(node: Node, key: String) = (node \ "@name").text == key
    ((node \ "Param" filter {
      n => filterAttribute(n, key)
    }) \ "@value").text
  }

  def getIntConfig(e: Node, key: String): Int = {
    getStringConfig(e, key).toInt
  }
}



