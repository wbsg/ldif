package ldif.modules.sieve.fusion

import functions.PassItOn
import ldif.util.Prefixes

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


/**
 * Contains a data fusion specification.
 * For each property in the input, the specification determines one fusion function and one output property name.
 * This specification is read from an XML file.
 *
 * @author Pablo Mendes
 * @author Hannes Muehleisen
 */

class FusionSpecification(val fusionFunctions: IndexedSeq[FusionFunction],
                          val outputPropertyNames: IndexedSeq[String],
                          val defaultFusionFunction: FusionFunction = new PassItOn) {

  assert(fusionFunctions.size == outputPropertyNames.size, "There should be one OutputPropertyName for each FusionFunction")

  override def toString(): String = {
    "FusionSpecification, functions= " + fusionFunctions + ", outputProp=" + outputPropertyNames + ", defaultFunc=" + defaultFusionFunction
  }

  override def equals(obj: Any) = {
    obj match {
      case ots: FusionSpecification => fusionFunctions.equals(ots.fusionFunctions) && outputPropertyNames.equals(ots.outputPropertyNames) && defaultFusionFunction.equals(ots.defaultFusionFunction)
      case _ => false
    }
  }
}

object FusionSpecification {

  /**
   * A <Class> node/elem will be passed in.
   */
  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty): FusionSpecification = {
    def createFusionFunctions(node: scala.xml.Node): FusionFunction = {
      val fusionClassName: String = (node \ "FusionFunction" \ "@class").text
      FusionFunction.create(fusionClassName, (node \ "FusionFunction").head)
    }

    def getOutputProperties(node: scala.xml.Node): String = {
      prefixes.resolve((node \ "@name").text)
    }

    val fusionFunctions = (node \ "Property").map(createFusionFunctions)
    val outputProperties = (node \ "Property").map(getOutputProperties)

    new FusionSpecification(fusionFunctions.toIndexedSeq, outputProperties.toIndexedSeq)

  }
}