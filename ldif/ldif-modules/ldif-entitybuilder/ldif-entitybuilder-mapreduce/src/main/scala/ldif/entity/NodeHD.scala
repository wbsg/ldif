package ldif.entity

import org.apache.hadoop.io.WritableComparable
import java.io.{DataInput, DataOutput}
import Node._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */

class NodeHD(private var value: String, private var datatypeOrLanguage: String, private var nodeType: Node.NodeType, private var graph: String) extends WritableComparable[NodeHD] {

  def compareTo(otherNode: NodeHD) = {
    // case: Both are Blank Nodes
    if(nodeType==BlankNode && otherNode.nodeType==BlankNode) {
      if(value!=otherNode.value)
        value.compare(otherNode.value)
      else
        graph.compare(otherNode.graph)
    } else if(nodeType==BlankNode || otherNode.nodeType==BlankNode) { // case: only one is a Blank Node
      if(nodeType==BlankNode)
        -1
      else
        1
    } else { // case: no Blank Nodes involved
      if(nodeType!=otherNode.nodeType)
        nodeType.id.compare(otherNode.nodeType.id)
      else if(value!=otherNode.value)
        value.compare(otherNode.value)
      else if(datatypeOrLanguage!=null && otherNode.datatypeOrLanguage!=null)
        datatypeOrLanguage.compare(otherNode.datatypeOrLanguage)
      else if(datatypeOrLanguage!=null && otherNode.datatypeOrLanguage==null)
        1
      else if(datatypeOrLanguage==null && otherNode.datatypeOrLanguage!=null)
        -1
      else
        0
    }
  }

  def readFields(input: DataInput) {
    value = input.readUTF
    datatypeOrLanguage = input.readUTF
    nodeType = Node.NodeTypeMap(input.readInt)
    graph = input.readUTF
  }

  def write(output: DataOutput) {
    output.writeUTF(value)
    output.writeUTF(datatypeOrLanguage)
    output.writeInt(nodeType.id)
    output.writeUTF(graph)
  }
}