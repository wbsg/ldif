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

package ldif.modules.sieve.fusion.functions

import org.slf4j.LoggerFactory
import ldif.modules.sieve.quality.QualityAssessmentProvider
import ldif.modules.sieve.fusion.FusionFunction
import ldif.entity.{Node, NodeTrait}
import ldif.util.{Consts, Prefixes}

/**
 * Fusion function that takes the average of all numeric input values for a given property.
 */

class Average extends FusionFunction("") {

	private val log = LoggerFactory.getLogger(getClass.getName)

	/**
	 * Picks the average value.
	 */
	override def fuse(patterns: Traversable[IndexedSeq[NodeTrait]], quality: QualityAssessmentProvider) : Traversable[IndexedSeq[NodeTrait]] = {
		var valuesSum : Double = 0
		var valuesCount : Int = 0
		var datatype : String = null
		var fusedNodes = IndexedSeq.empty[NodeTrait]

		if (patterns.nonEmpty) {
			patterns.foreach(
				nodes => nodes.foreach(
					node => {
						try {
							valuesSum += node.value.toDouble
							if (datatype==null && node.datatype!=null) {
								// Assume all values for the same pattern/property have the same datatype
								datatype = node.datatype
							}
							valuesCount += 1
						} catch {
							case e : NumberFormatException => {
								log.debug("Invalid value found while applying an Average fusion function: "+node.value+". Only numeric values are supported.")
							}
						}
					}
				)
			)
		}

		if(valuesCount > 0) {
			val avgValue = valuesSum/valuesCount
			fusedNodes =
				if (datatype == Consts.xsdInteger ||
					datatype == Consts.xsdLong ||
					datatype == Consts.xsdShort ||
					datatype == Consts.xsdNonNegativeInteger ||
					datatype == Consts.xsdNonPositveInteger ) {
					// Create int value node
					IndexedSeq(Node.createTypedLiteral(math.rint(avgValue).toInt.toString, datatype, "average"))
				} else {
					// Create floating point value node
					IndexedSeq(Node.createTypedLiteral(avgValue.toString, datatype, "average"))
				}
		}
		Traversable(fusedNodes)
	}

}

object Average {
	def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes) : FusionFunction = {
		new Average()
	}
}