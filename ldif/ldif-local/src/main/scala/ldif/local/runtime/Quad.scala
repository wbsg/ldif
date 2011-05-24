package ldif.local.runtime

import ldif.entity.Node

/**
 * An RDF quad.
 */
case class Quad(subject : Node, predicate : String, value : Node, graph : String) {
  def toNQuadFormat = {
    val sb = new StringBuilder
    sb ++= subject.toString
    sb ++= " <"
    sb ++= predicate
    sb ++= "> "
    sb ++= value.toString
    sb ++= " <"
    sb ++= graph
    sb ++= ">"
    sb.toString
  }
}

