/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk

import ldif.entity.{Restriction, Path, EntityDescription, Node}
import de.fuberlin.wiwiss.silk.util.convert.RestrictionConverter
import de.fuberlin.wiwiss.silk.entity.{EntityDescription => SilkEntityDescription}
import de.fuberlin.wiwiss.silk.entity.{Restriction => SilkRestriction}
import de.fuberlin.wiwiss.silk.config.Prefixes

/**
 * Converts a Silk EntityDescription to a LDFI EntityDescription.
 */
object ConvertEntityDescription {

  def apply(instanceSpec : SilkEntityDescription)(implicit prefixes : Prefixes = Prefixes.empty) : EntityDescription = {
    implicit val ldifPrefixes : ldif.util.Prefixes = prefixes.prefixMap

    val restriction = retrieveRestriction(instanceSpec)

    val paths = instanceSpec.paths.map(_.serialize).map(Path.parse).map(IndexedSeq(_)).toIndexedSeq

    EntityDescription(restriction, paths)
  }

  private def retrieveRestriction(instanceSpec : SilkEntityDescription)(implicit prefixes : Prefixes) = {
    val restrictionConverter = new RestrictionConverter

    val silkRestriction = restrictionConverter(instanceSpec.variable, instanceSpec.restrictions)

    Restriction(silkRestriction.operator.map(convertOperator))
  }

  private def convertOperator(silkOperator : SilkRestriction.Operator)(implicit prefixes : Prefixes) : Restriction.Operator = silkOperator match {
    case SilkRestriction.Condition(path, values) => Restriction.Condition(Path.parse(path.toString), values.map(value => Node.createUriNode(value, "")))
    case SilkRestriction.And(ops) => Restriction.And(ops.map(convertOperator))
    case SilkRestriction.Or(ops) => Restriction.Or(ops.map(convertOperator))
  }
}