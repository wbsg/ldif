package ldif.modules.sieve.fusion.functions

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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.util.{Consts, Prefixes}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.modules.sieve.quality.HashBasedQualityAssessment
import ldif.entity.{NodeTrait, Node}

/**
 * Tests the KeepFirst fusion function
 */
@RunWith(classOf[JUnitRunner])
class KeepFirstTest extends FlatSpec with ShouldMatchers {

	val fusionXml = <FusionFunction class="KeepFirst" metric="sieve:recency"/>
	val fusionObj = new KeepFirst(Consts.sievePrefix + "recency");

	it should "create the correct fusion function from XML" in {
		KeepFirst.fromXML(fusionXml)(Prefixes.stdPrefixes) should equal(fusionObj)
	}

	val bestNode = Node.createTypedLiteral("node1", Consts.xsdString, "graph1")
	val patterns = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			Node.createTypedLiteral("node2", Consts.xsdString, "graph2"),
			bestNode,
				Node.createTypedLiteral("node3", Consts.xsdString, "graph3")
		)
	)

	/**
	 * Create quality values
	 */
	val quality = new HashBasedQualityAssessment()
	quality.putScore(Consts.sievePrefix + "recency", "graph1", 0.5)
	quality.putScore(Consts.sievePrefix + "recency", "graph2", 0.2)
	quality.putScore(Consts.sievePrefix + "recency", "graph3", 0.3)

	it should "keep the highest scored value from a list of quality assessed values" in {
		val fused = fusionObj.fuse(patterns, quality)
		fused.size should equal(1)
		fused.head.size should equal(1)
		fused.head.head should equal(bestNode)
	}

}


