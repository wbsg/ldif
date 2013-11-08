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
import ldif.modules.sieve.quality.QualityAssessmentProvider
import ldif.modules.sieve.fusion.FusionFunction
import ldif.entity.NodeTrait
import ldif.util.Prefixes

/**
 * Fusion function that takes the maximum of all numeric input values for a given property.
 * @author volhabryl
 */

class Maximum extends FusionFunction("") {

  private val log = LoggerFactory.getLogger(getClass.getName)

  /**
   * Picks the max value.
   */
  override def fuse(patterns: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    var bestValue = IndexedSeq[NodeTrait]()
    var maxValue = 0.0
     if (patterns.nonEmpty) {
      bestValue = patterns.head
      patterns.foreach( nodes =>
        nodes.foreach( n =>{
          if (n.value.toDouble > maxValue) {
            maxValue = n.value.toDouble
            bestValue = IndexedSeq(n)
          }
        }))
    }
    Traversable(bestValue)
  }

}

object Maximum {
  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes) : FusionFunction = {
    new Maximum()
  }
}
