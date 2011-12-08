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

package ldif.hadoop.types

import ldif.entity.NodeWritable
import ldif.runtime.Quad
import org.apache.hadoop.io.{Writable, Text}
import java.io.{DataOutput, DataInput}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadWritable(var subject: NodeWritable, var property: Text, var obj: NodeWritable, var graph: Text) extends Writable with Serializable {
  def this() {this(new NodeWritable(), new Text(), new NodeWritable(), new Text())}

  def this(quad: Quad) {this(new NodeWritable(quad.subject), new Text(quad.predicate), new NodeWritable(quad.value), new Text(quad.graph))}

  def write(out: DataOutput) {
    subject.write(out)
    property.write(out)
    obj.write(out)
    graph.write(out)
  }

  def readFields(in: DataInput) {
    subject.readFields(in)
    property.readFields(in)
    obj.readFields(in)
    graph.readFields(in)
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append(subject.toNQuadsFormat).append(" <").append(property.toString).append("> ").append(obj.toNQuadsFormat).append(" <").append(graph.toString).append("> .").toString()
  }

  override def hashCode: Int = {
    var code = subject.value.hashCode()
    code = property.hashCode() + code * 31
    code = obj.value.hashCode() + code * 31
    code = graph.hashCode() + code * 31
    return code
  }

  override def equals(other: Any): Boolean = {
    if(!other.isInstanceOf[QuadWritable])
      return false
    val otherQuad = other.asInstanceOf[QuadWritable]
    if(subject==otherQuad.subject &&
       property.toString==otherQuad.property.toString &&
       obj==otherQuad.obj &&
       graph.toString==otherQuad.graph.toString)
      return true
    else
      return false
  }

  def asQuad = Quad(subject, property.toString, obj, graph.toString)
}