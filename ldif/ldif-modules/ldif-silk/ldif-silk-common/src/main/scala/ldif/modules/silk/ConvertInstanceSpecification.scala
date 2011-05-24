package ldif.modules.silk

import de.fuberlin.wiwiss.silk.instance.InstanceSpecification
import ldif.entity.Restriction._
import ldif.entity.{Restriction, Path, EntityDescription, Node}

/**
 * Converts a Silk InstanceSpecification to a LDFI EntityDescription.
 */
object ConvertInstanceSpecification extends (InstanceSpecification => EntityDescription)
{
  def apply(instanceSpec : InstanceSpecification) : EntityDescription =
  {
    val restriction =
      if(instanceSpec.restrictions.toSparql.trim == ".")
      {
        Restriction(None)
      }
      else
      {
        instanceSpec.restrictions.toSparql.split(" ") match
        {
          case Array(variable, predicate, value) =>
          {
            val path = Path.parse(variable + "/" + predicate)
            val cleanValue =  value.trim.stripPrefix("<").stripSuffix(">.")

            Restriction(Some(Condition(path, Set(Node.createUriNode(cleanValue, "")))))
          }
          case _ => throw new IllegalArgumentException("Unsupported restriction pattern")
        }
      }

    val paths = instanceSpec.paths.map(_.serialize).map(Path.parse).map(IndexedSeq(_)).toIndexedSeq

    EntityDescription(restriction, paths)

  }
}