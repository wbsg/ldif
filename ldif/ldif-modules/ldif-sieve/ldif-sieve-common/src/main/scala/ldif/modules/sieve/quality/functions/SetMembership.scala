package ldif.modules.sieve.quality.functions

/*
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.slf4j.LoggerFactory
import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.ScoringFunction

/**
 * Takes as input a set of acceptable strings and returns 1 if all input values belong to the set.
 */
class SetMembership(val set: Set[String]) extends ScoringFunction {

  assume(set.size>1)

  private val log = LoggerFactory.getLogger(getClass.getName)

  /**
   * Uses the first pattern and requires that all values for that pattern are within the set.
   */
  def score(graphId: NodeTrait, metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
    // assume there is only one pattern
    val indicator = metadataValues.head
    // require that all nodes belong
    indicator.foldLeft(1)( (acc,node) => {
      val thisNode = if (set.contains(node.value)) 1 else 0
      acc * thisNode
    })
  }

  override def toString() : String = {
      "SetMembership, set=" + set
  }

  override def equals(obj:Any) = {
    obj match {
      case tc: SetMembership => set == tc.set
      case _ => false
    }
  }
}

object SetMembership {
  def fromXML(node: scala.xml.Node): ScoringFunction = {
    try {
      val set: Set[String] = (node \ "Param" \ "@value").text.split(" ").toSet
      return new SetMembership(set)
    } catch {
      case ioe: Exception => throw new IllegalArgumentException("Error in set provided.")
    }
    return null;
  }
}
