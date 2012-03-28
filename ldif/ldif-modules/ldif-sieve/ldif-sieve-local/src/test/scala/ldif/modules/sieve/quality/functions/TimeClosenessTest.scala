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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import org.joda.time.{DateTime, DateTimeComparator, Days}

/**
 * 
 * @author pablomendes
 */
@RunWith(classOf[JUnitRunner])
class TimeClosenessTest extends FlatSpec with ShouldMatchers {

  val tcXml =   <ScoringFunction class="TimeCloseness">
                    <Param name="timeSpan" value="6"/>
                    <Input path="?GRAPH/provenance:lastUpdated"/>
                </ScoringFunction>


  val tcFunc = new TimeCloseness(6)
  val subject = Node.fromString("<subject>")

  val today = new DateTime()
  val todayMinus2 = new Node(today.minusDays(2).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
  val todayMinus3 = new Node(today.minusDays(3).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
  val todayMinus4 = new Node(today.minusDays(4).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")

  val nodes3 = Traversable(IndexedSeq(todayMinus3))
  val nodes23 = Traversable(IndexedSeq(todayMinus2,todayMinus3))
  val nodes32 = Traversable(IndexedSeq(todayMinus3,todayMinus2))

  val nodes43 = Traversable(IndexedSeq(todayMinus4,todayMinus3))
  val nodes234 = Traversable(IndexedSeq(todayMinus2,todayMinus3,todayMinus4))

  val nodesBad = Traversable(IndexedSeq(new Node("2012-sssssasas02-19T16:55:17Z", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")))
  val nodesEmptyValues = Traversable(IndexedSeq(new Node("", "", Node.TypedLiteral, "")))
  val nodesEmpty = Traversable(IndexedSeq())

  it should "return the correct implementation given XML" in {
    (TimeCloseness.fromXML(tcXml)) should equal (tcFunc)
  }

  it should "not fail with empty values" in {
    (tcFunc.score(subject,nodesEmptyValues)) should equal (0.0)
  }
  it should "not fail with empty set" in {
    (tcFunc.score(subject,nodesEmpty)) should equal (0.0)
  }

  it should "correctly score input values" in {
    (tcFunc.score(subject,nodes3)) should equal (1.0 - (3.0/6))
  }

  it should "correctly sort before scoring input values" in {
    (tcFunc.score(subject,nodes23)) should equal (tcFunc.score(subject,nodes32))
  }

  it should "sort and score values as expected" in {
    (tcFunc.score(subject,nodes23)) should equal (1.0 - (2.0/6))
    (tcFunc.score(subject,nodes43)) should equal (1.0 - (3.0/6))
    //(tcFunc.score(nodes234)) should equal (1.0 - (2.0/6))
  }

  it should "parse date" in {
    //val parser = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")
    val parser =  ISODateTimeFormat.dateTimeNoMillis();
    val startDate = parser.parseDateTime("2012-02-19T16:55:17Z")
    val endDate = parser.parseDateTime("2012-02-20T16:55:17Z")
    val d = Days.daysBetween(startDate, endDate).getDays;
    (d) should equal (1.0)
  }

  it should "survive an invalid date" in {
    (tcFunc.score(subject,nodesBad)) should equal (0.0)
  }

//  "ScoringFunctions" should "false/true" in {
//    (false) should equal  (true)
//  }

}