package ldif.modules.silk

import ldif.entity.Restriction.Condition
import de.fuberlin.wiwiss.silk.instance.InstanceSpecification
import ldif.entity.{Path, Restriction, EntityDescription}
import de.fuberlin.wiwiss.silk.linkspec.LinkSpecification
import de.fuberlin.wiwiss.silk.config.Prefixes

/**
 * Generates EntityDescriptions from a LinkSpecification.
 */
object CreateEntityDescriptions
{
  /**
   * Generates EntityDescriptions from a LinkSpecification.
   */
  def apply(linkSpec : LinkSpecification)(implicit prefixes : Prefixes) : Seq[EntityDescription] =
  {
    InstanceSpecification.retrieve(linkSpec).toSeq.map(ConvertInstanceSpecification.apply)
  }
}