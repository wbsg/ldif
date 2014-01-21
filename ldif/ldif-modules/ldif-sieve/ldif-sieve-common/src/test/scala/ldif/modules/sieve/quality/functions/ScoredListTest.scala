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
package ldif.modules.sieve.quality.functions

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node

@RunWith(classOf[JUnitRunner])
class ScoredListTest extends FlatSpec with ShouldMatchers {

	val slXml = <ScoringFunction class="ScoredList">
			<Param name="list" value="graphId1 graphId2 graphId3 graphId4"/>
	</ScoringFunction>

	val slFunc = new ScoredList(List("graphId1", "graphId2", "graphId3", "graphId4"))
	val graph1 = Node.fromString("<graphId1>")
	val graph2 = Node.fromString("<graphId2>")
	val graph3 = Node.fromString("<graphId3>")
	val graph4 = Node.fromString("<graphId4>")
	val graph5 = Node.fromString("<otherGraphId>")

	it should "return the correct implementation given XML" in {
		(ScoredList.fromXML(slXml)) should equal(slFunc)
	}

	it should "correctly score input values" in {
		(slFunc.score(graph1, null) should equal(1.0))
		(slFunc.score(graph2, null) should equal(0.75))
		(slFunc.score(graph3, null) should equal(0.5))
		(slFunc.score(graph4, null) should equal(0.25))
		(slFunc.score(graph5, null) should equal(0))
	}

}
