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

/**
 *
 * @author pablomendes
 */
@RunWith(classOf[JUnitRunner])
class AggregateFunctionsTest extends FlatSpec with ShouldMatchers {

	val tcFunc = new TimeCloseness(6)
	val subject = Node.fromString("<subject>")

	val node = new Node("", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
	val nodes = Traversable(IndexedSeq(node))

	it should "survive an invalid date" in {
		(tcFunc.score(subject, nodes)) should equal(0.0)
	}

}