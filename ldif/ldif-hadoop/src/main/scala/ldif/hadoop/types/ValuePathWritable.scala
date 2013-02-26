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

package ldif.hadoop.types

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, ArrayWritable, Writable}
import java.lang.Byte
import ldif.entity.{NodeTrait, NodeWritable}

case class ValuePathWritable (var pathID : IntWritable, var pathType: PathType, var values : NodeArrayWritable) extends Writable {

  def this() {this(new IntWritable(), EntityPathType, new NodeArrayWritable)}

  def write(output : DataOutput) {
    pathID.write(output)
    output.writeByte(pathType.bytePathType)
    values.write(output)
  }

  def readFields(input : DataInput) {
    pathID.readFields(input)
    pathType = PathTypeMap(input.readByte)
    values.readFields(input)
  }

  override def toString = {
    val builder = new StringBuilder
    val graph = values.get()(0).asInstanceOf[NodeTrait].graph
    builder.append(pathType.toString).append("(pathID=").append(pathID.toString).append(", graph=").append(graph).append(", ").append(values.toString).append(")")
    builder.toString
  }

  def length(): Int = {
    values.get.length-1
  }
}

sealed trait PathType {
  val bytePathType: Int
}

case object EntityPathType extends PathType {
  val bytePathType = 0
  override def toString = "EntityPathType"
}

case object JoinPathType extends PathType {
  val bytePathType = 1
  override def toString = "JoinPathType"
}

case object FinishedPathType extends PathType {
  val bytePathType = 2
  override def toString = "FinishedPathType"
}

case object PathTypeMap {
  val map = Map(0 -> EntityPathType, 1 -> JoinPathType, 2 -> FinishedPathType)
  def apply(index: Int) = map(index)
}