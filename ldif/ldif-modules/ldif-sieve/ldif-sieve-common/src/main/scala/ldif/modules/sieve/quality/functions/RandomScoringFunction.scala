package ldif.modules.sieve.quality.functions

import xml.Node
import ldif.modules.sieve.quality.ScoringFunction
import ldif.entity.{Entity, NodeTrait}

/**
 * Constructor of implementing classes should accept applicable Param and EnvironmentVariable values.
 * The values described in Input are passed at scoring time to the method "score".
 * @author pablomendes
 */

object RandomScoringFunction extends ScoringFunction {

  def fromXML(node: Node) = this

  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]) = Math.random

}

