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

package ldif.modules.silk.hadoop.io

import de.fuberlin.wiwiss.silk.cache.Partition
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import xml.XML
import java.io.{StringReader, DataOutput, DataInput}
import org.apache.hadoop.io.Writable

class PartitionWritable(var partition: Partition) extends Writable {

  def this() = this(null)

  def get = partition

  override def readFields(in: DataInput) {
    val entityDescStr = in.readUTF()
    val entityDesc = EntityDescription.fromXML(XML.load(new StringReader(entityDescStr)))
    partition = Partition.deserialize(in, entityDesc)
  }

  override def write(out: DataOutput) {
    val entityDesc = partition.entities.head.desc
    out.writeUTF(entityDesc.toXML.toString)
    partition.serialize(out)
  }
}