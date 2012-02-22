package ldif.modules.sieve.quality.functions

import ldif.entity.NodeTrait
import org.slf4j.LoggerFactory
import ldif.modules.sieve.quality.{ScoringFunctionConjunctive, ScoringFunction}


class IntervalMembership(val from : Int, val to: Int) extends ScoringFunctionConjunctive {
  assume(from < to)

  private val log = LoggerFactory.getLogger(getClass.getName)

  def scoreSingleValue(node: NodeTrait): Double = {
    // assume there is only one pattern
    try {
    val indicator : Int = node.value.toInt
     if (indicator >= from && indicator <= to)
       1.0
      else
       0.0
    } catch {
      case e: Exception => {
        log.debug("Error %s".format(e))
        0.0
      }
    }
  }

  override def toString() : String = {
    "IntervalMembership, interval [" + from + ","+ to + "]"
  }

  override def equals(obj:Any) = {
    obj match {
      case im: IntervalMembership => from == im.from &&  to == im.to
      case _ => false
    }
  }
}


object IntervalMembership {
  def fromXML(node: scala.xml.Node): ScoringFunction = {
    try {
      val start = ScoringFunction.getIntConfig(node,"from")
      val end = ScoringFunction.getIntConfig(node,"to")
      return new IntervalMembership(start,end)
    } catch {
      case ioe: Exception => throw new IllegalArgumentException("Error in interval provided.")
    }
    return null;
  }

}