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

class NodeWritable(var value: String, var datatypeOrLanguage: String, var nodeType: Node.NodeType, var graph: String) extends WritableComparable[NodeWritable] {
  def this(node: Node) {
    this(node.value, node.datatypeOrLanguage, node.nodeType, node.graph)
  }
  def compareTo(otherNode: NodeWritable) = {
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

  override def hashCode: Int = {
    var hash: Int = 1
    hash = hash * 31 + value.hashCode
    hash = hash * 31 + (if (datatypeOrLanguage == null) 0 else datatypeOrLanguage.hashCode)
    hash = hash * 31 + nodeType.hashCode
    if(nodeType==BlankNode)
      hash = hash * 31 + graph.hashCode
    hash
  }

  def toNTriplesFormat = nodeType match {
    case Literal => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\""
    case TypedLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"^^<" + NTriplesStringConverter.convertToEscapedString(datatypeOrLanguage) + ">"
    case LanguageLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + NTriplesStringConverter.convertToEscapedString(value) + ">"
  }

  override def toString = nodeType match {
    case Literal => "\"" + value + "\""
    case TypedLiteral => "\"" + value + "\"^^<" + datatypeOrLanguage + ">"
    case LanguageLiteral => "\"" + value + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + value + ">"
  }
}