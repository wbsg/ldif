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

package ldif.hadoop.types

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, WritableComparable}
import ldif.entity.NodeWritable

class EntityDescriptionNodeWritable(var entityDescriptionID : IntWritable, var node : NodeWritable) extends WritableComparable[EntityDescriptionNodeWritable]{

  def this() {
    this(new IntWritable(), new NodeWritable())
  }

  override def compareTo(other: EntityDescriptionNodeWritable): Int = {
    if(entityDescriptionID.compareTo(other.entityDescriptionID)!=0)
      return entityDescriptionID.compareTo(other.entityDescriptionID)
    else
      return node.compareTo(other.node)
  }

  def readFields(input: DataInput) {
    entityDescriptionID.readFields(input)
    node.readFields(input)
  }

  def write(output: DataOutput) {
    entityDescriptionID.write(output)
    node.write(output)
  }

  def set(entityDescriptionID: IntWritable,  node: NodeWritable) {
    this.entityDescriptionID = entityDescriptionID
    this.node = node
  }

  override def hashCode(): Int = {
    var hashCode = entityDescriptionID.hashCode
    hashCode = 31*hashCode + node.hashCode
    hashCode
  }
}