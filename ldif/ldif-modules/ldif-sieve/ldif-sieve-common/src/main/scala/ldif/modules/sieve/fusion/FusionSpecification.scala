package ldif.modules.sieve.fusion

import functions.{PassItOn, KeepUpToDate, TrustYourFriends}

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
class FusionSpecification(val id: String,
                          val fusionFunctions : IndexedSeq[FusionFunction],
                          val outputPropertyNames: IndexedSeq[String],
                          val defaultFusionFunction : FusionFunction = new PassItOn) {

  assert(fusionFunctions.size==outputPropertyNames.size, "There should be one OutputPropertyName for each FusionFunction")
    //val scoringFunctions = new PassItOn
    //val scoringFunctions = new KeepFirst
    //val scoringFunctions = new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
}

object FusionSpecification {

  /**
   * A <Class> node/elem will be passed in.
   */
  def fromXML(node: scala.xml.Node) = {
    //TODO implement as below
     //val specName = ...
     //val scoringFunctions = (node \ "Property").map(FusionFunction.fromXML)
     //val propertyNames = (node \ "Property").map(grabName)
     //new FusionSpecification(specName,scoringFunctions,propertyNames)

    //temporarily, hardcoded:
    createLwdm2012ExampleSpecs
  }

  def createLwdm2012ExampleSpecs = {
    new FusionSpecification("lwdm2012",
      IndexedSeq(new PassItOn,
        new TrustYourFriends("http://en.wikipedia.org.+"),
        new PassItOn,
        new KeepUpToDate("http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"),
        new PassItOn),
      IndexedSeq("http://www.w3.org/2000/01/rdf-schema#label",
        "http://dbpedia.org/ontology/areaTotal",
        "http://dbpedia.org/ontology/foundingDate",
        "http://dbpedia.org/ontology/populationTotal",
        "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    )
  }

  def createMusicExampleSpecs = {
    new FusionSpecification("test",
      IndexedSeq(new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en"), new PassItOn, new PassItOn, new PassItOn),
      IndexedSeq("http://www.w3.org/2000/01/rdf-schema#label", "http://xmlns.com/foaf/0.1/made", "http://www.w3.org/2002/07/owl#sameAs", "http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource")
    )

  }
}