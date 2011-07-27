package ldif.entity

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */

trait NodeTrait {
  def value : String
  def datatypeOrLanguage : String
  def nodeType : Node.NodeType
  def graph : String
}

//  sealed trait NodeType {val id: Int}
//
//  case object Literal extends NodeType { val id = 1}
//
//  case object TypedLiteral extends NodeType { val id = 2}
//
//  case object LanguageLiteral extends NodeType { val id = 3}
//
//  case object BlankNode extends NodeType { val id = 4}
//
//  case object UriNode extends NodeType { val id = 5}