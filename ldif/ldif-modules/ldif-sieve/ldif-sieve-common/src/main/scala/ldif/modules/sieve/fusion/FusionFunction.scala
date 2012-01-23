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

package ldif.modules.sieve.fusion

import ldif.entity.{NodeTrait, Node}
import ldif.modules.sieve.quality.QualityAssessmentProvider

/**
 * Interface for functions that perform data fusion
 * @author pablomendes
 */

trait FusionFunction {

  var name = classOf[FusionFunction].getClass.getSimpleName

  def sort (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  def filter (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  def combine (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  /**
   * Produces new property values from the fusion of the input values.
   * The default behavior is "do nothing", returning the input values unmodified.
   */
  def fuse(values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
    //TODO make generic: if only one value, already return that. otherwise sort, filter. test again. if >1 value, combine.
    values
  }

}

class PassItOn extends FusionFunction