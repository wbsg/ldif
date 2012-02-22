package ldif.modules.sieve.quality

import functions.{TimeCloseness, ScoredList}
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

object ScoringFunction {
  def create(className : String,  config: Node) : ScoringFunction = className.toLowerCase match {
     case "scoredlist" => return ScoredList.fromXML(config)
     case "timecloseness" => return TimeCloseness.fromXML(config)

     // NOTICE: add case statements for new scoring functions here
    case whatever => throw new IllegalArgumentException("Unable to construct scoring function for class name " + className)
  }
}



