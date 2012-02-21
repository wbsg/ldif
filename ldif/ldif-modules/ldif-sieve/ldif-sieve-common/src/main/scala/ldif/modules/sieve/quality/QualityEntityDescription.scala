package ldif.modules.sieve.quality

import ldif.util.Prefixes
import ldif.entity.{Node, Path, Restriction, EntityDescription}
import ldif.entity.Restriction.{Condition, Exists}

/**
 * Helper object to create LDIF entity descriptions out of the Quality Specification XML
 * Assumption: operates over resources of type ldif:ImportedGraph only. This tagging is done by LDIF at import time.
 * TODO Create one entity description from all assessment metrics, or one per assessment metric?
 */

object QualityEntityDescription {
  /**
   * A <Quality> node will be passed in
   * Need to grab pattern from all Input elements
   * TODO Initially consider only @path, later also need to consider @query
   */
  def fromXML(scoringFunction : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) = {
    val paths = ((scoringFunction \ "ScoringFunction" \ "Input" ).map(parsePath(_)(prefixes)))
    val restriction = Restriction.fromXML(<Restriction><Condition path="?a/rdf:type"><Uri>http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph</Uri></Condition></Restriction>)
    new EntityDescription(restriction,IndexedSeq(paths.toIndexedSeq))
  }

  def parsePath(pathNode : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty)  = {
    Path.parse((pathNode \ "@path" ).text)(prefixes)
  }
}