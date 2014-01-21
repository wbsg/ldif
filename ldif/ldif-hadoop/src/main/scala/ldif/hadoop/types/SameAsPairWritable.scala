/* 
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

package ldif.hadoop.types

import java.io.{DataOutput, DataInput}
import org.apache.hadoop.io.{Text, WritableComparable}
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairWritable(var from: String, var to: String, var iteration: Int) extends WritableComparable[SameAsPairWritable] {
  def this() {
    this(null, null, 0)
  }

  def write(out: DataOutput) {
    out.writeUTF(from)
    out.writeUTF(to)
    out.writeInt(iteration)
  }

  def readFields(in: DataInput) {
    from = in.readUTF()
    to = in.readUTF()
    iteration = in.readInt()
  }

  def compareTo(other: SameAsPairWritable) = {
    if(from==other.from)
      to.compareTo(other.to)
    else
      from.compareTo(other.from)
  }

  override def equals(other: Any): Boolean = {
    if(!other.isInstanceOf[SameAsPairWritable])
      return false
    else
      compareTo(other.asInstanceOf[SameAsPairWritable])==0
  }

  override def hashCode(): Int = {
    from.hashCode() + 31*to.hashCode()
  }

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append("<").append(from).append("> ")
    sb.append("<").append(Consts.SAMEAS_URI).append("> ")
    sb.append("<").append(to).append("> .")
    sb.toString()
  }
}
