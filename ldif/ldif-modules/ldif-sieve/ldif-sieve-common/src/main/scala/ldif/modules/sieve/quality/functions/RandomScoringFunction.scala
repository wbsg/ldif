package ldif.modules.sieve.quality.functions

import ldif.entity.NodeTrait
import xml.Node
import ldif.modules.sieve.quality.ScoringFunction

/**
 * Constructor of implementing classes should accept applicable Param and EnvironmentVariable values.
 * The values described in Input are passed at scoring time to the method "score".
 * @author pablomendes
 */

object RandomScoringFunction extends ScoringFunction {

  def fromXML(node: Node) = this

  def score(metadataValues: Traversable[IndexedSeq[NodeTrait]]) = Math.random

}

