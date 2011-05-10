package ldif.local.runtime

import ldif.entity.Node

/**
 * An RDF quad.
 */
case class Quad(subject : String, predicate : String, value : String, graph : String)

