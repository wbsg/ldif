package ldif.modules.silk

import ldif.entity.Restriction._
import ldif.entity.{Restriction, Path, EntityDescription, Node}
import de.fuberlin.wiwiss.silk.util.convert.RestrictionConverter
import de.fuberlin.wiwiss.silk.instance.InstanceSpecification
import de.fuberlin.wiwiss.silk.instance.{Restriction => SilkRestriction}
import de.fuberlin.wiwiss.silk.config.Prefixes

/**
 * Converts a Silk InstanceSpecification to a LDFI EntityDescription.
 */
object ConvertInstanceSpecification
{
  def apply(instanceSpec : InstanceSpecification)(implicit prefixes : Prefixes) : EntityDescription =
  {
    implicit val ldifPrefixes : ldif.util.Prefixes = prefixes.prefixMap

    val restriction = retrieveRestriction(instanceSpec)

    val paths = instanceSpec.paths.map(_.serialize).map(Path.parse).map(IndexedSeq(_)).toIndexedSeq

    EntityDescription(restriction, paths)
  }

  private def retrieveRestriction(instanceSpec : InstanceSpecification)(implicit prefixes : Prefixes) =
  {
    val restrictionConverter = new RestrictionConverter

    val silkRestriction = restrictionConverter(instanceSpec.variable, instanceSpec.restrictions)

    Restriction(silkRestriction.operator.map(convertOperator))
  }

  private def convertOperator(silkOperator : SilkRestriction.Operator)(implicit prefixes : Prefixes) : Restriction.Operator = silkOperator match
  {
    case SilkRestriction.Condition(path, values) => Restriction.Condition(Path.parse(path.toString), values.map(value => Node.createUriNode(value, "")))
    case SilkRestriction.And(ops) => Restriction.And(ops.map(convertOperator))
    case SilkRestriction.Or(ops) => Restriction.Or(ops.map(convertOperator))
  }
}