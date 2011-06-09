package ldif.local.runtime

import ldif.entity.Node
import ldif.util.NTriplesStringConverter

/**
 * An RDF quad.
 */
case class Quad(subject : Node, predicate : String, value : Node, graph : String) {
  def toNQuadFormat = {
    val sb = new StringBuilder
    sb ++= subject.toNTriplesFormat
    sb ++= " <"
    sb ++= NTriplesStringConverter.convertToEscapedString(predicate)
    sb ++= "> "
    sb ++= value.toNTriplesFormat
    sb ++= " <"
    sb ++= NTriplesStringConverter.convertToEscapedString(graph)
    sb ++= ">"
    sb.toString
  }
}

