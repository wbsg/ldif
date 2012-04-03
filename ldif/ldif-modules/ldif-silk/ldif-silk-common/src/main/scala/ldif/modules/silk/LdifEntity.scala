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

package ldif.modules.silk

import de.fuberlin.wiwiss.silk.entity.{Entity => SilkEntity}
import de.fuberlin.wiwiss.silk.entity.{EntityDescription => SilkEntityDescription}
import ldif.entity.{FactumBuilder, Entity}

/**
 * A Silk instance which can be viewed as a LDIF entity.
 */
class LdifEntity(val entity : Entity, entityDesc : SilkEntityDescription, factumBuilder : FactumBuilder = null)
    extends SilkEntity(entity.resource.value, IndexedSeq.tabulate(entityDesc.paths.size)(i => entity.factums(i,factumBuilder).map(_.last.value).toSet), entityDesc)
{
}

object LdifEntity {
  implicit def toEntity(entity : Entity) = entity.asInstanceOf[LdifEntity].entity
}
