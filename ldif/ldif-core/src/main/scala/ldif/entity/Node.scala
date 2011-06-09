package ldif.entity

import Node._
import org.semanticweb.yars.nx.parser.NxParser
import ldif.util.NTriplesStringConverter

final case class Node protected(val value : String, datatypeOrLanguage : String, val nodeType : Node.NodeType, val graph : String)
{
  def datatype = nodeType match
  {
    case TypedLiteral => datatypeOrLanguage
    case _ => null
  }

  def language = nodeType match
  {
    case LanguageLiteral => datatypeOrLanguage
    case _ => null
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

  def modifyGraph(graph: String): Node = {
    new Node(this.value, this.datatypeOrLanguage, this.nodeType, graph)
  }

  override def equals(other: Any): Boolean = {
    if (this.asInstanceOf[AnyRef] eq other.asInstanceOf[AnyRef])
      true
    if (!(other.isInstanceOf[Node]))
      false
    else {
      var otherNode: Node = other.asInstanceOf[Node]
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

  override def hashCode: Int = {
    var hash: Int = 1
    hash = hash * 31 + value.hashCode
    hash = hash * 31 + (if (datatypeOrLanguage == null) 0 else datatypeOrLanguage.hashCode)
    hash = hash * 31 + nodeType.hashCode
    if(nodeType==BlankNode)
      hash = hash * 31 + graph.hashCode
    hash
  }

  def toXML =  nodeType match {
    //TODO Literal language and datatype not supported in M1
      case Literal => <Literal>{value}</Literal>
      case TypedLiteral => <Literal>{value}</Literal>
      case LanguageLiteral =>  <Literal>{value}</Literal>
      case BlankNode => <BlankNode>{value}</BlankNode>
      case UriNode => <Uri>{value}</Uri>
  }

  override def toString = nodeType match {
    case Literal => "\"" + value + "\""
    case TypedLiteral => "\"" + value + "\"^^<" + datatypeOrLanguage + ">"
    case LanguageLiteral => "\"" + value + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + value + ">"
  }

  def toNTriplesFormat = nodeType match {
    case Literal => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\""
    case TypedLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"^^<" + NTriplesStringConverter.convertToEscapedString(datatypeOrLanguage) + ">"
    case LanguageLiteral => "\"" + NTriplesStringConverter.convertToEscapedString(value) + "\"@" + datatypeOrLanguage
    case BlankNode => "_:"+ value
    case UriNode => "<" + NTriplesStringConverter.convertToEscapedString(value) + ">"
  }

}

object Node
{
  val defaultGraph : String = "default"

  def createLiteral(value : String, graph : String) = new Node(value, null, Literal, graph)

  def createTypedLiteral(value : String, datatype : String, graph : String) = new Node(value, datatype, TypedLiteral, graph)

  def createLanguageLiteral(value : String, language : String, graph : String) = new Node(value, language, LanguageLiteral, graph)

  def createBlankNode(value : String, graph : String) = new Node(value, null, BlankNode, graph)

  def createUriNode(value : String, graph : String) = new Node(value, null, UriNode, graph)

  def fromString(value : String, graph : String) = {
    val nxNode = NxParser.parseNode(value)
    fromNxNode(nxNode,graph)
  }

  def fromString(value : String) : Node = fromString(value,defaultGraph)

  def fromNxNode(nxNode : org.semanticweb.yars.nx.Node, graph : String) = {
     nxNode match {
      case lit:org.semanticweb.yars.nx.Literal => {
        val dt = lit.getDatatype
        val lang = lit.getLanguageTag
        val value = lit.getData
        if (dt!=null)
          ldif.entity.Node.createTypedLiteral(value,dt.toString,graph)
        else if (lang!=null)
          ldif.entity.Node.createLanguageLiteral(value,lang,graph)
        else ldif.entity.Node.createLiteral(value,graph)
      }
      case bno:org.semanticweb.yars.nx.BNode =>
        ldif.entity.Node.createBlankNode(bno.toString,graph)
      case res:org.semanticweb.yars.nx.Resource =>
        ldif.entity.Node.createUriNode(res.toString,graph)
    }
  }

  def fromNxNode(nxNode : org.semanticweb.yars.nx.Node) : Node = fromNxNode(nxNode,defaultGraph)

  // Build node from an XML node
  def fromXML(xml : scala.xml.Node) : Node = xml match  {
    //TODO Literal language and datatype not supported in M1
    case <Uri>{value @ _*}</Uri> => createUriNode(value.text,defaultGraph)
    case <Literal>{value @ _*}</Literal> => createLiteral(value.text,defaultGraph)
    case <BlankNode>{value @ _*}</BlankNode> => createBlankNode(value.text,defaultGraph)
  }

  sealed trait NodeType

  case object Literal extends NodeType

  case object TypedLiteral extends NodeType

  case object LanguageLiteral extends NodeType

  case object BlankNode extends NodeType

  case object UriNode extends NodeType
}
