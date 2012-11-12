/*
 * LDIF
 *
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