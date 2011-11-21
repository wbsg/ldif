package ldif.entity

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */

import Node.{BlankNode, Literal, UriNode, LanguageLiteral, TypedLiteral}
import ldif.util.{MD5Helper, NTriplesStringConverter}

trait NodeTrait {
  def value : String
  def datatypeOrLanguage : String
  def nodeType : Node.NodeType
  def graph : String

  def compare(otherNode: NodeTrait) = {
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

  override def hashCode: Int = {
    return value.hashCode
  }

  override def equals(other: Any): Boolean = {
    if (this.asInstanceOf[AnyRef] eq other.asInstanceOf[AnyRef])
      true
    if (!(other.isInstanceOf[NodeTrait]))
      false
    else {
      var otherNode: NodeTrait = other.asInstanceOf[NodeTrait]
      var result = (otherNode.nodeType == nodeType) && compareDTorLang(this.datatypeOrLanguage, otherNode.datatypeOrLanguage) && (this.value.equals(otherNode.value))
      if(nodeType== BlankNode)
        result = result && (graph == otherNode.graph)
      result
    }
  }

  private def compareDTorLang(v1: String, v2: String): Boolean = {
    if (v1 == null)
      v2 == null
    else v1.equals(v2)
  }

  override def toString = nodeType match {
    case Literal => "\"" + value + "\""
    case TypedLiteral => "\"" + value + "\"^^<" + datatypeOrLanguage + ">"
    case LanguageLiteral => "\"" + value + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + value + ">"
  }

  def toNQuadsFormat = nodeType match {
    case Literal => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\""
    case TypedLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"^^<" + NTriplesStringConverter.convertToEscapedString(datatypeOrLanguage) + ">"
    case LanguageLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + NTriplesStringConverter.convertToEscapedString(value) + ">"
  }

  def toNTriplesFormat = nodeType match {
    case Literal => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\""
    case TypedLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"^^<" + NTriplesStringConverter.convertToEscapedString(datatypeOrLanguage) + ">"
    case LanguageLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"@" + datatypeOrLanguage
    case BlankNode => "_:g" + MD5Helper.md5(graph) + value
    case UriNode => "<" + NTriplesStringConverter.convertToEscapedString(value) + ">"
  }

  def isResource = {
    if(nodeType==UriNode || nodeType==BlankNode)
      true
    else
      false
  }

  def isUriNode = {
    nodeType==UriNode
  }

  def isBlankNode = {
    nodeType==BlankNode
  }
}