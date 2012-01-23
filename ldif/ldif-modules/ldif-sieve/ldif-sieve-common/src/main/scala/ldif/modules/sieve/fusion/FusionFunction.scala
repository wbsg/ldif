package ldif.modules.sieve.fusion

import ldif.entity.{NodeTrait, Node}
import ldif.modules.sieve.QualityAssessment

/**
 * Interface for functions that perform data fusion
 * @author pablomendes
 */

trait FusionFunction {

  var name = classOf[FusionFunction].getClass.getSimpleName

  def sort (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  def filter (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  def combine (values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    values
  }

  /**
   * Produces new property values from the fusion of the input values.
   * The default behavior is "do nothing", returning the input values unmodified.
   */
  def fuse(values: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessment) : Traversable[IndexedSeq[NodeTrait]] = {
    //TODO make generic: if only one value, already return that. otherwise sort, filter. test again. if >1 value, combine.
    values
  }

}

class PassItOn extends FusionFunction