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
import org.apache.hadoop.io.{WritableComparable, Writable}

class IndexWritable(var indices: Set[Int]) extends WritableComparable[IndexWritable] {

  def this() = this(null)

  //TODO improve
  override def compareTo(other: IndexWritable) = {
    if(this.indices == other.indices)
      0
    else
      1//indices.size.compareTo(other.indices.size)
  }

  override def readFields(in: DataInput) {
    indices = Array.fill(in.readInt())(in.readInt()).toSet
  }

  override def write(out: DataOutput) {
    out.writeInt(indices.size)
    for(i <- indices)
      out.writeInt(i)
  }
}