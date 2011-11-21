/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.runtime

import ldif.util.NTriplesStringConverter
import java.util.Comparator
import ldif.entity.NodeTrait

/**
 * An RDF quad.
 */
case class Quad(subject : NodeTrait, predicate : String, value : NodeTrait, graph : String) {
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

  def toLine = toNQuadFormat + " . \n"
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