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
package ldif.modules.sieve.fusion.functions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.{NodeTrait, Node}
import ldif.util.{Consts, Prefixes}

/**
 * Tests the Voting fusion function
 */
@RunWith(classOf[JUnitRunner])
class VotingTest extends FlatSpec with ShouldMatchers {

	val fusionXml = <FusionFunction class="Voting"/>
	val votingFusionObj = new Voting;

	it should "create the correct fusion function from XML" in {
		Voting.fromXML(fusionXml)(Prefixes.empty) should equal(votingFusionObj)
	}

	val bestNode = Node.createTypedLiteral("pickMe!", Consts.xsdString, "graph3")
	val patterns = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			bestNode,
			Node.createTypedLiteral("node2", Consts.xsdString, "graph2"),
			Node.createTypedLiteral("pickMe!", Consts.xsdString, "graph4"),
			Node.createTypedLiteral("node1", Consts.xsdString, "graph1")
		)
	)

	it should "correctly pick the most voted value" in {
		val fused = votingFusionObj.fuse(patterns, null)
		fused.size should equal(1)
		fused.head.size should equal(1)
		fused.head.head should equal(bestNode)
	}

}


