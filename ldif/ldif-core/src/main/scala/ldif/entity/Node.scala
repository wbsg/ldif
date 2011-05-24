package ldif.entity

import Node._


final case class Node protected(val value : String, datatypeOrLanguage : String, val nodeType : Node.NodeType, val graph : String)
{
  def datatype = nodeType match
  {
    case Node.TypedLiteral => datatypeOrLanguage
    case _ => null
  }

  def language = nodeType match
  {
    case Node.LanguageLiteral => datatypeOrLanguage
    case _ => null
  }

  def isResource = {
    if(nodeType==Node.UriNode || nodeType==Node.BlankNode)
      true
    else
      false
  }

  def isUriNode = {
    nodeType==Node.UriNode
  }

  override def equals(other: Any): Boolean = {
    if (this.asInstanceOf[AnyRef] eq other.asInstanceOf[AnyRef]) true
    if (!(other.isInstanceOf[Node])) false
    var otherNode: Node = other.asInstanceOf[Node]
    var result = (otherNode.nodeType == nodeType) && compareDTorLang(this.datatypeOrLanguage, otherNode.datatypeOrLanguage) && (this.value.equals(otherNode.value))
    if(nodeType== BlankNode)
      result = result && (graph == otherNode.graph)
    result
  }

  private def compareDTorLang(v1: String, v2: String): Boolean = {
    if (v1 == null)
      v2 == null
    else v1.equals(v2)
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


  override def toString = nodeType match {
    case Literal => "\"" + value + "\""
    case TypedLiteral => "\"" + value + "\"^^<" + datatypeOrLanguage + ">"
    case LanguageLiteral => "\"" + value + "\"@" + datatypeOrLanguage
    case BlankNode => value
    case UriNode => "<" + value + ">"
  }
}

object Node
{
  def createLiteral(value : String, graph : String) = new Node(value, null, Literal, graph)

  def createTypedLiteral(value : String, datatype : String, graph : String) = new Node(value, datatype, TypedLiteral, graph)

  def createLanguageLiteral(value : String, language : String, graph : String) = new Node(value, language, LanguageLiteral, graph)

  def createBlankNode(value : String, graph : String) = new Node(value, null, BlankNode, graph)

  def createUriNode(value : String, graph : String) = new Node(value, null, UriNode, graph)

  def fromString(value: String, graph : String) = {
    //TODO analyse value and create the proper node 
    if (value.startsWith("_:"))
      createBlankNode(value,"default")
    else if (value.startsWith("http://"))
      createUriNode(value,"default")
    else createLiteral(value,"default")
  }
  
  sealed trait NodeType

  case object Literal extends NodeType

  case object TypedLiteral extends NodeType

  case object LanguageLiteral extends NodeType

  case object BlankNode extends NodeType

  case object UriNode extends NodeType


}
