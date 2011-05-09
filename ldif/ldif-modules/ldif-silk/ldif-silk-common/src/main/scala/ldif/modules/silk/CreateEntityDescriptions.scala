package ldif.modules.silk

import ldif.entity.Restriction.Condition
import de.fuberlin.wiwiss.silk.instance.InstanceSpecification
import ldif.entity.{Path, Restriction, EntityDescription}
import de.fuberlin.wiwiss.silk.linkspec.LinkSpecification

/**
 * Generates EntityDescriptions from a LinkSpecification.
 */
object CreateEntityDescriptions extends (LinkSpecification => Seq[EntityDescription])
{
  /**
   * Generates EntityDescriptions from a LinkSpecification.
   */
  def apply(linkSpec : LinkSpecification) : Seq[EntityDescription] =
  {
    InstanceSpecification.retrieve(linkSpec).toSeq.map(convert)
  }

  /**
   * Converts a Silk InstanceSpecification to a LDFI EntityDescription
   */
  private def convert(instanceSpec : InstanceSpecification) : EntityDescription =
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