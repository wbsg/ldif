package ldif.entity

class Factum private(val value : String, datatypeOrLanguage : String, val nodeType : Factum.NodeType, graph : String)
{
  def datatype = nodeType match
  {
    case Factum.TypedLiteral => datatypeOrLanguage
    case _ => null
  }

  def language = nodeType match
  {
    case Factum.LanguageLiteral => datatypeOrLanguage
    case _ => null
  }
}

object Factum
{
  def createLiteral(value : String, graph : String) = new Factum(value, null, Literal, graph)

  def createTypedLiteral(value : String, datatype : String, graph : String) = new Factum(value, datatype, TypedLiteral, graph)

  def createLanguageLiteral(value : String, language : String, graph : String) = new Factum(value, language, LanguageLiteral, graph)

  def createBlankNode(value : String, graph : String) = new Factum(value, null, BlankNode, graph)

  def createUriNode(value : String, graph : String) = new Factum(value, null, UriNode, graph)

  sealed trait NodeType

  case object Literal extends NodeType

  case object TypedLiteral extends NodeType

  case object LanguageLiteral extends NodeType

  case object BlankNode extends NodeType

  case object UriNode extends NodeType
}