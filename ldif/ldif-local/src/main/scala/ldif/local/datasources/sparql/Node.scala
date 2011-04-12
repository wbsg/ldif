package ldif.local.datasources.sparql

/**
 * An RDF node, which is one of: ResourceNode, BlankNode, Literal.
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
case class ResourceNode(value : String) extends Node

/**
 * An RDF blank node.
 */
case class BlankNode(value : String) extends Node

/**
 * An RDF literal.
 */
case class Literal(value : String) extends Node
