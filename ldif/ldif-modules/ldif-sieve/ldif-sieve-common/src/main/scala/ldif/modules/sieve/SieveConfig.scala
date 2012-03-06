package ldif.modules.sieve

import fusion.FusionConfig
import quality.QualityConfig
import xml.XML
import ldif.util.{Prefixes, ValidatingXMLReader}
import java.io.File
import org.slf4j.LoggerFactory

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

class SieveConfig(val qualityConfig: QualityConfig, val fusionConfig: FusionConfig) {

}

object SieveConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def reader = new ValidatingXMLReader(fromSieveXmlConfig, "Sieve.xsd")

  def fromSieveXmlConfig(configFile: File) = {
    val sieveConfig = XML.loadFile(configFile)
    val prefixes = Prefixes.fromXML(sieveConfig \ "Prefixes" head)
    val qualityConfig = (sieveConfig \ "QualityAssessment").map(QualityConfig.fromXML(_)(prefixes))
    val fusionConfig = (sieveConfig \ "Fusion").map(FusionConfig.fromXML(_)(prefixes))
    new SieveConfig(qualityConfig.head, fusionConfig.head) //TODO could we have more than one, e.g. multiple files?
  }

  def load(configFile: File): SieveConfig = {
    reader.apply({
      configFile
    })
  }

  /**
   * This is a helper method to inform R2R of all the properties used in Sieve,
   * so that it can create workaround IdentityMappings to pass all necessary properties to Sieve.
   */
  def getUsedProperties(configFile: File) = {
    val sieveConfig = XML.loadFile(configFile)
    val prefixes = Prefixes.fromXML(sieveConfig \ "Prefixes" head) ++ Prefixes.stdPrefixes
    val qualityProperties = (sieveConfig \ "QualityAssessment" \ "AssessmentMetric").map( n => prefixes.resolve((n \ "@id").text ))
    val fusionProperties = (sieveConfig \\ "Fusion" \\ "Class" \\ "Property").map(n => prefixes.resolve((n \ "@name").text ))
    val r = (qualityProperties ++ fusionProperties)
    log.trace("Used properties: %s".format(r.toString()))
    r
  }

  def main(args: Array[String]) {

    SieveConfig.getUsedProperties(new File("ldif/examples/rio/sieve/rio-config.xml"))
  }
}
