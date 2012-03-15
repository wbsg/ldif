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

package ldif.modules.sieve.fusion.functions
import org.slf4j.LoggerFactory
import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.QualityAssessmentProvider
import ldif.modules.sieve.fusion.FusionFunction
import ldif.util.Prefixes

/**
 * Fusion function that keeps all the best rated values according to a given quality assessment metric.
 */

class KeepValuesWithHighestScore(metricId: String) extends FusionFunction(metricId) {

  private val log = LoggerFactory.getLogger(getClass.getName)

  /**
   * Picks the values with the highest quality assessment with one pass over all nodes in all patterns in input.
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
          else if (score == bestScore) {
            bestValue = bestValue :+ n
          }
      }))
    }
    Traversable(bestValue)
  }

}

object KeepValuesWithHighestScore {

  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes) : FusionFunction = {
    val metricQName = (node \ "@metric").text
    if (metricQName.isEmpty)
      throw new IllegalArgumentException("Function %s needs the attribute 'metric' to be included in the tag FusionFunction.".format(KeepValuesWithHighestScore.getClass))
    val metricId = prefixes.resolve(metricQName)
    new KeepValuesWithHighestScore(metricId)
  }
}