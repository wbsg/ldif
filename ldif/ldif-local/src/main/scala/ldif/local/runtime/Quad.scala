package ldif.local.runtime

import ldif.entity.Node

/**
 * An RDF quad.
 */
case class Quad(subject : Node, predicate : String, value : Node, graph : String)

