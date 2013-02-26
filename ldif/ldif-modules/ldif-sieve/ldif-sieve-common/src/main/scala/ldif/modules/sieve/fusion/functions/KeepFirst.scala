package ldif.modules.sieve.fusion.functions

/*
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

import org.slf4j.LoggerFactory
import ldif.entity.NodeTrait
import ldif.modules.sieve.fusion.FusionFunction
import ldif.modules.sieve.quality.{ScoringFunction, QualityAssessmentProvider}
import ldif.util.Prefixes

/**
 * Fusion function that keeps the best rated value according to a given quality assessment metric.
 *
 * @author pablomendes
 */

class KeepFirst(metricId: String) extends FusionFunction(metricId) {

  private val log = LoggerFactory.getLogger(getClass.getName)

  /**
   * Picks the value with the highest quality assessment with one pass over all nodes in all patterns in input.
   */
  override def fuse(patterns: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    var bestValue = IndexedSeq[NodeTrait]()
    if (patterns.nonEmpty) {
      bestValue = patterns.head
      var bestScore = 0.0
      patterns.foreach( nodes =>
        nodes.foreach( n =>{
          val score = quality.getScore(metricId, n.graph)
          if (score > bestScore) {
            bestScore = score
            bestValue = IndexedSeq(n)
          }
      }))
    }
    Traversable(bestValue)
  }

}

object KeepFirst {

  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes) : FusionFunction = {
    val metricQName = (node \ "@metric").text
    if (metricQName.isEmpty)
      throw new IllegalArgumentException("Function %s needs the attribute 'metric' to be included in the tag FusionFunction.".format(KeepFirst.getClass))
    val metricId = prefixes.resolve(metricQName)
    new KeepFirst(metricId)
  }
}