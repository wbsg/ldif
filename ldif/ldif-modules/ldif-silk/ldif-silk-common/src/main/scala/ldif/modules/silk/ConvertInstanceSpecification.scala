package ldif.modules.silk

import de.fuberlin.wiwiss.silk.instance.InstanceSpecification
import ldif.entity.Restriction._
import ldif.entity.{Restriction, Path, EntityDescription}

/**
 * Converts a Silk InstanceSpecification to a LDFI EntityDescription.
 */
object ConvertInstanceSpecification extends (InstanceSpecification => EntityDescription)
{
  def apply(instanceSpec : InstanceSpecification) : EntityDescription =
  {
    val restriction = instanceSpec.restrictions.toSparql.split(" ") match
    {
      case Array(variable, predicate, value) =>
      {
        val path = Path.parse(variable + "/" + predicate)
        val cleanValue =  value.trim.stripPrefix("<").stripSuffix(">.")

        Restriction(Some(Condition(path, Set(cleanValue))))
      }
      case _ => throw new IllegalArgumentException("Unsupported restriction pattern")
    }

    val paths = instanceSpec.paths.map(_.serialize).map(Path.parse).map(IndexedSeq(_)).toIndexedSeq

    EntityDescription(restriction, paths)
  }
}