package ldif.modules.sieve.quality

import functions.{ScoredRegexList, RandomScoringFunction,ScoredList,TimeCloseness}
import ldif.util.Prefixes
import collection.IndexedSeq
import scala.Predef._
import ldif.entity.EntityDescription

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
 * @author Pablo Mendes
 * @author Hannes Mühleisen
 */
class QualitySpecification(val id: String,
                           val scoringFunctions: IndexedSeq[ScoringFunction],
                           val outputPropertyNames: IndexedSeq[String],
                           val entityDescription : EntityDescription) {
}

object QualitySpecification {

  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty) = {
    val id = (node \ "@id").text
    val scoringClassName = (node \ "ScoringFunction" \ "@class").text.toLowerCase
    val scoringInstance = ScoringFunction.create(scoringClassName,(node \ "ScoringFunction" head))
    val scoringFunctions = IndexedSeq[ScoringFunction](scoringInstance);
    val outputPropertyNames = IndexedSeq((prefixes++Prefixes.stdPrefixes).resolve(id))

    val entityDescription = QualityEntityDescription.fromXML(node)(prefixes)

    new QualitySpecification(id, scoringFunctions, outputPropertyNames, entityDescription)
  }
}