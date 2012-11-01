/*
 * LDIF
 *
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

package ldif.modules.sieve.quality.functions

import ldif.entity.NodeTrait
import org.slf4j.LoggerFactory
import ldif.modules.sieve.quality.{ScoringFunctionConjunctive, ScoringFunction}


class IntervalMembership(val from : Int, val to: Int) extends ScoringFunctionConjunctive {
  assume(from < to)

  private val log = LoggerFactory.getLogger(getClass.getName)

  def scoreSingleValue(node: NodeTrait): Double = {
    // assume there is only one pattern
    try {
    val indicator : Int = node.value.toInt
     if (indicator >= from && indicator <= to)
       1.0
      else
       0.0
    } catch {
      case e: Exception => {
        log.debug("Error %s".format(e))
        0.0
      }
    }
  }

  override def toString() : String = {
    "IntervalMembership, interval [" + from + ","+ to + "]"
  }

  override def equals(obj:Any) = {
    obj match {
      case im: IntervalMembership => from == im.from &&  to == im.to
      case _ => false
    }
  }
}


object IntervalMembership {
  def fromXML(node: scala.xml.Node): ScoringFunction = {
    try {
      val start = ScoringFunction.getIntConfig(node,"from")
      val end = ScoringFunction.getIntConfig(node,"to")
      return new IntervalMembership(start,end)
    } catch {
      case ioe: Exception => throw new IllegalArgumentException("Error in interval provided.")
    }
    return null
  }

}