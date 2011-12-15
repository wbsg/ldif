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

package ldif.modules.silk.hadoop

import java.io.{DataOutput, DataInput}
import ldif.entity.EntityWritable
import org.apache.hadoop.io.Writable

class EntityPairWritable(var source: EntityWritable, var target: EntityWritable) extends Writable {
  require(source != null, "source != null")
  require(target != null, "target != null")

  def this() = this(new EntityWritable(), new EntityWritable())

  override def readFields(in: DataInput) {
    source.readFields(in)
    target.readFields(in)
  }

  override def write(out: DataOutput) {
    source.write(out)
    target.write(out)
  }
}