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
    InstanceSpecification.retrieve(linkSpec).toSeq.map(ConvertInstanceSpecification)
  }
}