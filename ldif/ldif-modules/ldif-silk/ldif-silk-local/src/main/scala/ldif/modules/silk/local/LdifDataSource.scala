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

package ldif.modules.silk.local

import de.fuberlin.wiwiss.silk.datasource.DataSource
import ldif.local.runtime.EntityReader
import ldif.modules.silk.LdifEntity
import de.fuberlin.wiwiss.silk.entity.{Entity => SilkEntity}
import de.fuberlin.wiwiss.silk.entity.{EntityDescription => SilkEntityDescription}

/**
 * Silk DataSource which reads the entities from an EntityReader.
 */
case class LdifDataSource(reader : EntityReader) extends DataSource {

  override def retrieve(entityDesc : SilkEntityDescription, instances : Seq[String] = Seq.empty) = new Traversable[SilkEntity] {
    def foreach[U](f: SilkEntity => U) {
      while(reader.hasNext) {
        f(new LdifEntity(reader.read(), entityDesc, reader.factumBuilder))
      }
    }
  }
}