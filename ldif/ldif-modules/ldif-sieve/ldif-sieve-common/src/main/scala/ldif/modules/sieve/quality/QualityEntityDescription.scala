package ldif.modules.sieve.quality

import ldif.util.Prefixes
import ldif.entity.{Path, Restriction, EntityDescription}

/**
 * Helper object to create LDIF entity descriptions out of the Quality Specification XML
 * Since indicators come from graphs,
 * Create one entity description from all assessment metrics.
 */

object QualityEntityDescription {
  /**
   * A <Quality> node will be passed in
   * Need to grab pattern from all Input elements
   * TODO Initially consider only @path, later also need to consider @query
   */
  def fromXML(scoringFunction : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) = {
    val paths = ((scoringFunction \ "ScoringFunction" \ "Input" ).map(parsePath(_)(prefixes)))
    new EntityDescription(Restriction(None),IndexedSeq(paths.toIndexedSeq))
  }

  def parsePath(pathNode : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty)  = {
    Path.parse((pathNode \ "@path" ).text)(prefixes)
  }
}