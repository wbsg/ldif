package ldif.entity

import org.apache.hadoop.io.WritableComparable
import java.io.{DataInput, DataOutput}
import Node._
import ldif.util.NTriplesStringConverter

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */

class NodeWritable(var value: String, var datatypeOrLanguage: String, var nodeType: Node.NodeType, var graph: String) extends NodeTrait with WritableComparable[NodeWritable] {
  def this(node: Node) {
    this(node.value, node.datatypeOrLanguage, node.nodeType, node.graph)
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