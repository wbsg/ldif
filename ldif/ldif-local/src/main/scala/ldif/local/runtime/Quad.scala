package ldif.local.runtime

/**
 * An RDF quad.
 */
case class Quad(subject : String, predicate : String, value : Node, graph : String)

/**
 * An RDF node, which is one of: Resource, BlankNode, Literal.
 */
sealed trait Node
{
  /**
   * The value of this node.
   */
  val value : String
}

/**
 * An RDF resource.
 */
case class Resource(value : String) extends Node

/**
 * An RDF blank node.
 */
case class BlankNode(value : String) extends Node

/**
 * An RDF literal.
 */
case class Literal(value : String, lang : String, datatype : String) extends Node
