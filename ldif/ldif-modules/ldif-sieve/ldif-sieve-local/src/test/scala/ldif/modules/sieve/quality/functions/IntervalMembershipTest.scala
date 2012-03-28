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

@RunWith(classOf[JUnitRunner])
class IntervalMembershipTest extends FlatSpec with ShouldMatchers {

  val imXml = <ScoringFunction class="IntervalMembership">
      <Param name="from" value="6"/>
      <Param name="to" value="42"/>
      <Input path="?GRAPH/provenance:whatever"/>
  </ScoringFunction>

  val imFunc = new IntervalMembership(6, 42)
  val subject = Node.fromString("<subject>")
  val inValue = new Node("8", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val highOutValue = new Node("52", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val lowOutValue = new Node("2", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val lowerBorderValue = new Node("6", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val upperBorderValue = new Node("42", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")

  val bogusValue = new Node("foo", "http://www.w3.org/2001/XMLSchema#bar", Node.TypedLiteral, "graphId")


  it should "return the correct implementation given XML" in {
    (IntervalMembership.fromXML(imXml)) should equal(imFunc)
  }

  it should "correctly score input values" in {
    (imFunc.score(subject, Traversable(IndexedSeq((inValue)))) should equal(1.0))
    (imFunc.score(subject, Traversable(IndexedSeq((lowerBorderValue)))) should equal(1.0))
    (imFunc.score(subject, Traversable(IndexedSeq((upperBorderValue)))) should equal(1.0))

    (imFunc.score(subject, Traversable(IndexedSeq((highOutValue)))) should equal(0.0))
    (imFunc.score(subject, Traversable(IndexedSeq((lowOutValue)))) should equal(0.0))
  }

  it should "only accept valid configuration" in {
    evaluating {
      new IntervalMembership(100, 10)
    } should produce[AssertionError]
    evaluating {
      new IntervalMembership(10, 10)
    } should produce[AssertionError]
  }

  it should "survive an invalid input" in {
    (imFunc.score(subject,Traversable(IndexedSeq((bogusValue))))) should equal (0.0)
  }

}