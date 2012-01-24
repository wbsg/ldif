package ldif.modules.sieve.quality

import ldif.entity.NodeTrait
import xml.Node

/**
 * Constructor of implementing classes should accept applicable Param and EnvironmentVariable values.
 * The values described in Input are passed at scoring time to the method "score".
 * @author pablomendes
 */

trait ScoringFunction {

  /**
   * Providing as input a list of nodes in an entity description, compute
   */
  def score(metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double

  /**
   * Builds an object of type ScoringFunction based on an XML description
   */
  def fromXML(node: Node) : ScoringFunction

}

object RandomScoringFunction extends ScoringFunction {

  def fromXML(node: Node) = this

  def score(metadataValues: Traversable[IndexedSeq[NodeTrait]]) = Math.random

}