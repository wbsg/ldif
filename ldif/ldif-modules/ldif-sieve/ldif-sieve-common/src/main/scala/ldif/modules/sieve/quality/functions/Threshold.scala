package ldif.modules.sieve.quality.functions

import ldif.entity.NodeTrait
import org.slf4j.LoggerFactory
import ldif.modules.sieve.quality.{ScoringFunctionConjunctive, ScoringFunction}

class Threshold(val ts: Int) extends ScoringFunctionConjunctive {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def scoreSingleValue(node: NodeTrait): Double = {
    try {
      val indicator: Int = node.value.toInt
      if (indicator >= ts)
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

  override def toString(): String = {
    "Threshold, ts= " + ts
  }

  override def equals(obj: Any) = {
    obj match {
      case ots: Threshold => ts == ots.ts
      case _ => false
    }
  }
}

object Threshold {
  def fromXML(node: scala.xml.Node): ScoringFunction = {
    try {
      val ts = ScoringFunction.getIntConfig(node, "threshold")
      return new Threshold(ts)
    } catch {
      case ioe: Exception => throw new IllegalArgumentException("Error in threshold provided.")
    }
    return null;
  }
}