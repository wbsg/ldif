package ldif.modules.silk

import ldif.entity.Entity
import de.fuberlin.wiwiss.silk.instance.{InstanceSpecification, Instance}

/**
 * A Silk instance which can be viewed as a LDIF entity.
 */
class LdifInstance(val entity : Entity, instanceSpec : InstanceSpecification)
    extends Instance(entity.uri, IndexedSeq.fill(instanceSpec.paths.size)(i => entity.factums(i).map(_.last.value).toSet), instanceSpec)
{
}

object LdifInstance
{
  implicit def toEntity(instance : Instance) = instance.asInstanceOf[LdifInstance].entity
}
