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
 * @author pablomendes
 */
class QualityConfig(val prefixes: Prefixes,
                   val qualitySpecs: Traversable[QualitySpecification],
                   val entityDescriptions: Seq[EntityDescription]) {

  def merge(c: QualityConfig) : QualityConfig = { //TODO implement
    throw new NotImplementedException
    //this
  }

}

object QualityConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)
  private val schemaLocation = "de/fuberlin/wiwiss/sieve/Sieve.xsd"

  val stdPrefixes = Map("foaf" -> "http://xmlns.com/foaf/0.1/",
    "dbpedia-owl" -> "http://dbpedia.org/ontology/",
    //"dbpedia" -> "http://dbpedia.org/resource/",
    "genes"->"http://wiking.vulcan.com/neurobase/kegg_genes/resource/vocab/",
    "smwprop"->"http://mywiki/resource/property/",
    "smwcat"->"http://mywiki/resource/category/",
    "wiki"->"http://www.example.com/smw#",
    "ldif"->"http://www4.wiwiss.fu-berlin.de/ldif/",
    "xsd" -> "http://www.w3.org/2001/XMLSchema#",
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#",
    "owl" -> "http://www.w3.org/2002/07/owl#")

  def load(configFile: File) : QualityConfig = {
    load(new FileInputStream(configFile))
  }

  /**
   * Gameplan for loading from XML is:
   *  -- grab Prefixes tag, build prefixes
   *  -- start from Quality tag.
   *  ---- for each AssessmentMetric tag, create:
   *         an entity description with all paths in Input
   *         a quality spec pairing each property with a scoring function.
   *           --- use ScoringFunction.fromXML to create it.
   *
   */
  def load(configFile: InputStream) : QualityConfig = {
    //TODO use reader below
    //new ValidatingXMLReader(fromXML, schemaLocation)

    //temporarily
    val qualitySpecs = List(QualitySpecification.createLwdm2012ExampleSpecs)
    val entityDescriptions = List(EntityDescription.fromXML(QualityEntityDescription.createLwdm2012EntityDescription)(stdPrefixes))
    new QualityConfig(new Prefixes(stdPrefixes), qualitySpecs, entityDescriptions)
  }

  //TODO untested. this is just a stub to guide implementation, needs to be realized
  def fromXML(node: scala.xml.Node) = {
    implicit val prefixes = Prefixes.fromXML(node \ "Prefixes" head)
    val specs = (node \ "Quality" ).map(QualitySpecification.fromXML)
    val entityDescriptions = (node \ "Quality").map(QualityEntityDescription.fromXML(_)(prefixes))
    new QualityConfig(prefixes, specs, entityDescriptions)
  }

  def empty = new EmptyQualityConfig
}

/*
 This class should never be actually used for fusion. It simply signals that no config exists, and the framework should repeat the input.
 */
class EmptyQualityConfig extends QualityConfig(Prefixes.stdPrefixes,
                                List(new QualitySpecification("Empty",
                                                             IndexedSeq(RandomScoringFunction),
                                                             IndexedSeq("DEFAULT")
                                                             )),
                                List(EntityDescription.empty)

) {

}

  /*
<http://dbpedia.org/ontology/musicalArtist>
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
<http://www.w3.org/2000/01/rdf-schema#label>
<http://www.w3.org/2002/07/owl#sameAs>
<http://xmlns.com/foaf/0.1/made>
<http://xmlns.com/foaf/0.1/member>

provenance triples:
<http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportType>
<http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation>
<http://www4.wiwiss.fu-berlin.de/ldif/importId>
<http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate>

   */
