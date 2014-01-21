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
package ldif.modules.sieve.fusion.functions

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.{NodeTrait, Node}
import ldif.util.{Consts, Prefixes}

/**
 * Tests the Average fusion function
 */
//@RunWith(classOf[JUnitRunner])
class AverageTest extends FlatSpec with ShouldMatchers {

	val fusionXml = <FusionFunction class="Average"/>
	val averageFusionObj = new Average;

	it should "create the correct fusion function from XML" in {
		Average.fromXML(fusionXml)(Prefixes.empty) equals averageFusionObj
	}

	// Test with double values
	val patternsDouble = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			Node.createTypedLiteral("2.3", Consts.xsdDouble),
			Node.createTypedLiteral("2.6", Consts.xsdDouble)
		))

	it should "correctly pick the average value (double)" in {
		val fused = averageFusionObj.fuse(patternsDouble, null)
		fused.size should equal(1)
		fused.head.size should equal(1)
		fused equals Node.createTypedLiteral("2.6", Consts.xsdFloat)
	}

	// Test with int values
	val patternsInt = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			Node.createTypedLiteral("1", Consts.xsdInteger),
			Node.createTypedLiteral("5", Consts.xsdInteger)
		))

	it should "correctly pick the average value (integer)" in {
		val fused = averageFusionObj.fuse(patternsInt, null)
		fused.size should equal(1)
		fused.head.size should equal(1)
		fused.head.head should equal(Node.createTypedLiteral("3", Consts.xsdInteger))
	}

	// Test with invalid values
	val patternsInvalid = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			Node.createLiteral("not numeric value"),
			Node.createTypedLiteral("not numeric value", Consts.xsdInteger)
		))

	it should "correctly pick the average value (invalid)" in {
		val fused = averageFusionObj.fuse(patternsInvalid, null)
		fused.head.size should equal(0)
	}

	// Test with both invalid and valid values
	val patternsValidAndInvalid = Traversable[IndexedSeq[NodeTrait]](
		IndexedSeq(
			Node.createLiteral("not numeric value"),
			Node.createTypedLiteral("5", Consts.xsdInteger)
		))

	it should "correctly pick the average value (valid and invalid)" in {
		val fused = averageFusionObj.fuse(patternsValidAndInvalid, null)
		fused.size should equal(1)
		fused.head.size should equal(1)
		fused.head.head should equal(Node.createTypedLiteral("5", Consts.xsdInteger))
	}

}
