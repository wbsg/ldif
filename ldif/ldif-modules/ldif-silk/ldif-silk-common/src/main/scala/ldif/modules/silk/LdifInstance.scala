/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import ldif.entity.Entity
import de.fuberlin.wiwiss.silk.instance.{InstanceSpecification, Instance}

/**
 * A Silk instance which can be viewed as a LDIF entity.
 */
class LdifInstance(val entity : Entity, instanceSpec : InstanceSpecification)
    extends Instance(entity.resource.value, IndexedSeq.tabulate(instanceSpec.paths.size)(i => entity.factums(i).map(_.last.value).toSet), instanceSpec)
{
}

object LdifInstance
{
  implicit def toEntity(instance : Instance) = instance.asInstanceOf[LdifInstance].entity
}
