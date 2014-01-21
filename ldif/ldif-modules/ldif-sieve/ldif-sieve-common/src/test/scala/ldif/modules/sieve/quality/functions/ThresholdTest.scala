package functions

/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node
import ldif.modules.sieve.quality.functions.Threshold

@RunWith(classOf[JUnitRunner])
class ThresholdTest extends FlatSpec with ShouldMatchers {

	val imXml = <ScoringFunction class="Threshold">
			<Param name="min" value="42"/>
			<Input path="?GRAPH/provenance:whatever"/>
	</ScoringFunction>

	val tsFunc = new Threshold(42)
	val subject = Node.fromString("<subject>")
	val above = new Node("52", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
	val below = new Node("2", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
	val bogusValue = new Node("foo", "http://www.w3.org/2001/XMLSchema#bar", Node.TypedLiteral, "graphId")


	it should "return the correct implementation given XML" in {
		(Threshold.fromXML(imXml)) should equal(tsFunc)
	}

	it should "correctly score input values" in {
		(tsFunc.score(subject, Traversable(IndexedSeq(above))) should equal(1.0))
		(tsFunc.score(subject, Traversable(IndexedSeq(below))) should equal(0.0))
	}

	it should "survive an invalid input" in {
		(tsFunc.score(subject, Traversable(IndexedSeq(bogusValue)))) should equal(0.0)
	}

}
