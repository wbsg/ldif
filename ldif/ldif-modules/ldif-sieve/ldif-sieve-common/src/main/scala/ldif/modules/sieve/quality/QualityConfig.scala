package ldif.modules.sieve.quality

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

import functions.RandomScoringFunction
import java.io.{FileInputStream, InputStream, File}
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import ldif.entity.EntityDescription
import org.slf4j.LoggerFactory
import ldif.util.Prefixes

/**
 * Quality Assessment configuration.
 * For each "Quality" element in the XML config, contains
 *   -- one entity description for all Inputs as a pattern
 *   -- one quality spec mapping a scoring function to an assessment metric id to be output.
 *
 * @author Pablo Mendes
 * @author Hannes Mühleisen
 */
class QualityConfig(val name: String, val description: String, val prefixes: Prefixes,
                    val qualitySpecs: Traversable[QualitySpecification],
                    val entityDescriptions: Seq[EntityDescription]) {

  def merge(c: QualityConfig): QualityConfig = {
    //TODO implement (for what?)(if user gives many files?)
    throw new NotImplementedException
    //this
  }
}

object QualityConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)



  def fromXML(node: scala.xml.Node)(implicit prefixes: Prefixes = Prefixes.empty) = {
    val configName = (node \ "@name").text
    val configDesc = (node \ "@description").text
    val specs = (node \\ "AssessmentMetric").map(QualitySpecification.fromXML(_)(prefixes))
    val entityDescriptions = (node \\ "AssessmentMetric").map(QualityEntityDescription.fromXML(_)(prefixes))
    new QualityConfig(configName, configDesc, prefixes, specs, entityDescriptions)
    // TODO: handle aggregate Metrics (next version)
  }

  def empty = new EmptyQualityConfig
}

/*
 This class should never be actually used for fusion. It simply signals that no config exists, and the framework should repeat the input.
 */
class EmptyQualityConfig extends QualityConfig("", "", Prefixes.stdPrefixes,
  List(new QualitySpecification("Empty",
    IndexedSeq(RandomScoringFunction),
    IndexedSeq("DEFAULT")
  )),
  List(EntityDescription.empty)

) {
}