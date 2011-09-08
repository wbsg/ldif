package ldif.runtime

import ldif.entity.Node
import ldif.util.NTriplesStringConverter
import java.util.Comparator

/**
 * An RDF quad.
 */
case class Quad(subject : Node, predicate : String, value : Node, graph : String) {
  def toNQuadFormat = {
    val sb = new StringBuilder
    sb ++= subject.toNQuadsFormat
    sb ++= " <"
    sb ++= NTriplesStringConverter.convertToEscapedString(predicate)
    sb ++= "> "
    sb ++= value.toNQuadsFormat
    sb ++= " <"
    sb ++= NTriplesStringConverter.convertToEscapedString(graph)
    sb ++= ">"
    sb.toString
  }

  def toNTripleFormat = {
    val sb = new StringBuilder
    sb ++= subject.toNTriplesFormat
    sb ++= " <"
    sb ++= NTriplesStringConverter.convertToEscapedString(predicate)
    sb ++= "> "
    sb ++= value.toNTriplesFormat
    sb.toString
  }

  override def equals(other: Any): Boolean = {
    if (this.asInstanceOf[AnyRef] eq other.asInstanceOf[AnyRef])
      true
    if (!(other.isInstanceOf[Quad]))
      false
    else {
      val otherQuad = other.asInstanceOf[Quad]
      subject==otherQuad.subject && predicate==otherQuad.predicate && value==otherQuad.value && graph==otherQuad.graph
    }
  }
}

class ForwardComparator extends Comparator[Quad] {
  def compare(left: Quad, right: Quad) = {
    if(left.subject!=right.subject)
      left.subject.compare(right.subject)
    else if(left.predicate!=right.predicate)
      left.predicate.compareTo(right.predicate)
    else if(left.value!=right.value)
      left.value.compare(right.value)
    else
      left.graph.compareTo(right.graph)
  }
}

class BackwardComparator extends Comparator[Quad] {
  def compare(left: Quad, right: Quad) = {
    if(left.value!=right.value)
      left.value.compare(right.value)
    else if(left.predicate!=right.predicate)
      left.predicate.compareTo(right.predicate)
    else if(left.subject!=right.subject)
      left.subject.compare(right.subject)
    else
      left.graph.compareTo(right.graph)
  }
}