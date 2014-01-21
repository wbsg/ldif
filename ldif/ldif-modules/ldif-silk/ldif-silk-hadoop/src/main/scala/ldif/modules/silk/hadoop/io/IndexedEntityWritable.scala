/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk.hadoop.io

import de.fuberlin.wiwiss.silk.entity.{EntityDescription, Index, Entity}
import xml.XML
import java.io.{DataOutput, StringReader, DataInput}
import ldif.entity.EntityWritable
import de.fuberlin.wiwiss.silk.cache.{BitsetIndex, Partition}
import org.apache.hadoop.io.Writable

class IndexedEntityWritable(var entity: EntityWritable, var index: BitsetIndex) extends Writable {

  def this() = this(new EntityWritable(), null)

  override def readFields(in: DataInput) {
    entity.readFields(in)
    index = BitsetIndex.deserialize(in)
  }

  override def write(out: DataOutput) {
    entity.write(out)
    index.serialize(out)
  }

}