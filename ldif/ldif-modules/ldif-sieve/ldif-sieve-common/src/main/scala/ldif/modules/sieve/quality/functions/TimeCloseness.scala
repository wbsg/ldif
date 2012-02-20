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
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Days, DateTimeComparator, DateTime}

/**
 * Takes as input a date and outputs a real valued assessment of how close that date is to now, given a dateRange as normalizer.
 * The formula is:
 * 1 - ((today - lastUpdate) / dateRange)
 *
 * The parameter dateRange can be configured at instantiation time.
 *
 * Can be used to compute freshness of content (if applied to lastUpdate), or newness of content (if applied to creationDate).
 *
 * TODO can be generalized to accept days, hours, seconds, etc.
 *
 * @author pablomendes
 */

class TimeCloseness(val dateRange: Int = 30) extends ScoringFunction {

  assume(dateRange>1)

  private val log = LoggerFactory.getLogger(getClass.getName)

  val parser = ISODateTimeFormat.dateTimeNoMillis();
  val ordering = Ordering.fromLessThan[DateTime](DateTimeComparator.getInstance.compare(_,_) < 0)

  def score(metadataValues: Traversable[IndexedSeq[NodeTrait]]): Double = {
    //assume the input has only one pattern
    val indicator = metadataValues.head
    // indicator may contain several dates
    indicator.map( node => {
          try {
            val originalDate = parser.parseDateTime(node.value) //TODO check which type of date is passed in.
            val currentDate = new DateTime
            val daysSince = Days.daysBetween(originalDate, currentDate).getDays;
            if (daysSince > dateRange)
              0.0
            else
              1 - (daysSince.toDouble / dateRange)
          } catch {
            case _ => 0.0
          }
        })
      .sorted.reverse.head // pick the most recent date

          //.sorted(ordering) //in case of many "last" updates, sort and get the latest one.
//          .headOption match {
//            case Some(originalDate) =>  {
//
//            }
//            case None => 0
//          }
  }

  override def toString() : String = {
      "TimeCloseness, timespan=" + dateRange
  }

  override def equals(obj:Any) = {
    obj match {
      case tc: TimeCloseness => dateRange == tc.dateRange
      case _ => false
    }
  }
}

object TimeCloseness {
  def fromXML(node: scala.xml.Node) : ScoringFunction = {
    val range : Int =  (node \ "Param" \ "@value").text.toInt
    if (range < 0) {
      throw new IllegalArgumentException("No positive value given for range")
    }
    new TimeCloseness(range)
  }
}
