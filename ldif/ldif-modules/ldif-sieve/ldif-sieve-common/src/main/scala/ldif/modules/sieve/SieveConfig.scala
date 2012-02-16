package ldif.modules.sieve

import fusion.FusionConfig
import quality.QualityConfig
import xml.XML
import ldif.util.{Prefixes, ValidatingXMLReader}
import java.io.{FileInputStream, File}

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

class SieveConfig(val qualityConfig: QualityConfig, val fusionConfig : FusionConfig) {

}

object SieveConfig {

  val stdPrefixes = Map("foaf" -> "http://xmlns.com/foaf/0.1/",
    "dbpedia-owl" -> "http://dbpedia.org/ontology/",
    "dbpedia" -> "http://dbpedia.org/resource/",
    "genes"->"http://wiking.vulcan.com/neurobase/kegg_genes/resource/vocab/",
    "smwprop"->"http://mywiki/resource/property/",
    "smwcat"->"http://mywiki/resource/category/",
    "wiki"->"http://www.example.com/smw#",
    "ldif"->"http://www4.wiwiss.fu-berlin.de/ldif/",
    "xsd" -> "http://www.w3.org/2001/XMLSchema#",
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#",
    "owl" -> "http://www.w3.org/2002/07/owl#")

  // TODO: allow the schema to be somewhere else? proposal: no
  def reader = new ValidatingXMLReader(fromSieveXmlConfig, "Sieve.xsd")

  def fromSieveXmlConfig(configFile: File) = {
    val sieveConfig = XML.loadFile(configFile)
    val prefixes = Prefixes.fromXML(sieveConfig \ "Prefixes" head)
    val qualityConfig = (sieveConfig \ "QualityAssessment" ).map(QualityConfig.fromXML(_)(prefixes))
    val fusionConfig = (sieveConfig \ "Fusion" ).map(FusionConfig.fromXML(_)(prefixes))
    new SieveConfig(qualityConfig(0),fusionConfig(0))
  }

  def load(configFile: File): SieveConfig = {
   reader.apply({configFile})
  }

}
