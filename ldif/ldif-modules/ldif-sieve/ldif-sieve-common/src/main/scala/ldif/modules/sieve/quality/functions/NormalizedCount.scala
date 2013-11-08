package ldif.modules.sieve.quality.functions

/*
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.ScoringFunction
import org.slf4j.LoggerFactory
import ldif.entity.NodeTrait
import ldif.modules.sieve.quality.ScoringFunction
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Days, DateTimeComparator, DateTime}

/**
 * Takes as input a numeric count and outputs a real valued assessment of how close it to the maxCount
 * The formula is:
 * if (count > maxCount) 1.0
 * else count/maxCount
 *
 * Can be used to compare numeric counts (e.g. edit count).
 *
 * TODO check for type consistency
 *
 * @author volhabryl
 */

class NormalizedCount(val maxCount: Int = 30) extends ScoringFunction {

  assume(maxCount>0)

  private val log = LoggerFactory.getLogger(getClass.getName)

  //// val parser = ISODateTimeFormat.dateTimeNoMillis(); // ???
  //// val ordering = Ordering.fromLessThan[DateTime](DateTimeComparator.getInstance.compare(_,_) < 0) // ???

  /**
   * Takes first pattern. Scores and sorts all counts in the values for that pattern, returns top score.
   */
  def score(graphId:NodeTrait,metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
    try {

      if (metadataValues.size==0)
        return 0.0

      val indicator = metadataValues.head //assume the input has only one pattern TODO enable for many patterns as well, requiring that they are all numeric
      if (indicator.size==0)
      {
        log.trace("No indicators found for graph %s.".format(graphId.value)) //tmp
        return 0.0
      }

      indicator.map( node => { // indicator may contain several dates
        try {
          val value = node.value.toDouble //TODO check which data type is passed in.
          if (value > maxCount)
            1.0
          else
            value/maxCount
        } catch {
          case _ => 0.0
        }
      }).sorted.reverse.head // pick the most recent date (i.e. the lowest score)

    } catch {
      case e: Exception => { // if it fails for any unanticipated reason, yell, but do not fail with a whimp.
        log.error("Error %s".format(e))
        0.0
      }
    }
  }

  override def toString() : String = {
    "NormalizedCount, maxCount=" + maxCount
  }

  override def equals(obj:Any) = {
    obj match {
      case tc: NormalizedCount => maxCount == tc.maxCount
      case _ => false
    }
  }
}

object NormalizedCount {
  def fromXML(node: scala.xml.Node): ScoringFunction = {
    try {
      val range: Int = (node \ "Param" \ "@value").text.toInt
      if (range < 0) {
        throw new IllegalArgumentException("No positive value given for range")
      }
      return new NormalizedCount(range)
    } catch {
      case ioe: NumberFormatException => throw new IllegalArgumentException("No positive value given for range")
    }
    return null;
  }
}

