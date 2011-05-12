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
    createUriNode(value,null)
  }
  
  sealed trait NodeType

  case object Literal extends NodeType

  case object TypedLiteral extends NodeType

  case object LanguageLiteral extends NodeType

  case object BlankNode extends NodeType

  case object UriNode extends NodeType


}
