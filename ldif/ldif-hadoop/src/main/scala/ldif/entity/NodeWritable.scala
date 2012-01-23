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

package ldif.entity

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.WritableComparable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */

class NodeWritable(var value: String, var datatypeOrLanguage: String, var nodeType: Node.NodeType, var graph: String) extends NodeTrait with WritableComparable[NodeWritable] {
  def this(node: NodeTrait) {
    this(node.value, node.datatypeOrLanguage, node.nodeType, node.graph)
  }

  def this(value: String) {
    this(Node.createUriNode(value, ""))
  }

  def this() {
    this(null, null, null, null)
  }

  def readFields(input: DataInput) {
    value = input.readUTF
    val dtOL = input.readUTF()
    if(dtOL=="")
      datatypeOrLanguage = null
    else
      datatypeOrLanguage = dtOL
    nodeType = Node.NodeTypeMap(input.readInt)
    graph = input.readUTF
  }

  def write(output: DataOutput) {
    output.writeUTF(value)
    if(datatypeOrLanguage!=null)
      output.writeUTF(datatypeOrLanguage)
    else
      output.writeUTF("")
    output.writeInt(nodeType.id)
    output.writeUTF(graph)
  }

  override def compareTo(other: NodeWritable) = super.compare(other)
}