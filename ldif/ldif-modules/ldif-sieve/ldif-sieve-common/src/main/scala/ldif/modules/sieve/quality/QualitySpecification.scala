package ldif.modules.sieve.quality

import functions.{ScoredRegexList, RandomScoringFunction}

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
 * @author pablomendes
 */
class QualitySpecification(val id: String,
                          val scoringFunctions : IndexedSeq[ScoringFunction],
                          val outputPropertyNames: IndexedSeq[String]
                          ) {

  assert(scoringFunctions.size==outputPropertyNames.size, "There should be one OutputPropertyName for each ScoringFunction")
    //val scoringFunctions = new PassItOn
    //val scoringFunctions = new KeepFirst
    //val scoringFunctions = new ScoredList("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
}

object QualitySpecification {

  /**
   * A <Class> node/elem will be passed in.
   */
  def fromXML(node: scala.xml.Node) = {
    //TODO implement as below
     //val specName = ...
     //val scoringFunctions = (node \ "Property").map(FusionFunction.fromXML)
     //val propertyNames = (node \ "Property").map(grabName)
     //new QualitySpecification(specName,scoringFunctions,propertyNames)

    //temporarily, hardcoded:
    createLwdm2012ExampleSpecs
  }

  def createLwdm2012ExampleSpecs = {
    new QualitySpecification("lwdm2012-qa",
      IndexedSeq(new ScoredRegexList(List("http://dbpedia.org.+"))), //List("http://en.+","http://pt.+"))),
      IndexedSeq("http://ldif/reputation")
    )
  }

  def createMusicExampleSpecs = {
    new QualitySpecification("test",
      IndexedSeq(),
      IndexedSeq()
    )
  }
}