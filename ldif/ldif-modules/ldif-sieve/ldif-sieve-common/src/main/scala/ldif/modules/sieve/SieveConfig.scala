package ldif.modules.sieve

import fusion.FusionConfig
import quality.QualityConfig
import xml.XML
import ldif.util.{Prefixes, ValidatingXMLReader}
import org.slf4j.LoggerFactory
import java.io.{FilenameFilter, File}
import collection.mutable.{HashSet,Set}

/*
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

class SieveConfig(val qualityConfig: QualityConfig, val fusionConfig: FusionConfig) {

}

object SieveConfig {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def reader = new ValidatingXMLReader(fromSieveXmlConfig, "Sieve.xsd")

  def fromSieveXmlConfig(configFile: File) = {
    val sieveConfig = XML.loadFile(configFile)
    val prefixes = Prefixes.fromXML(sieveConfig \ "Prefixes" head)
    val qualityConfig = (sieveConfig \ "QualityAssessment").map(QualityConfig.fromXML(_)(prefixes)).headOption match {
      case Some(config) => config
      case None => QualityConfig.empty
    }
    val fusionConfig = (sieveConfig \ "Fusion").map(FusionConfig.fromXML(_)(prefixes)).headOption match {
      case Some(config) => config
      case None => FusionConfig.empty
    }
    new SieveConfig(qualityConfig, fusionConfig) //TODO could we have more than one, e.g. multiple files?
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
  def getUsedProperties(configFile: File): Set[String] = {
    if(configFile==null)
      return Set[String]()
    val realConfigFile: File = {
      if(configFile.isDirectory) {
        val files = configFile.list(new FilenameFilter {
          override def accept(dir: File, name: String): Boolean = {
            return name.toLowerCase.endsWith(".xml")
          }
        })
        if(files.length>0)
          new File(configFile, files(0))
        else
          return Set[String]()
      }
      else
        configFile
    }
    val sieveConfig = XML.loadFile(realConfigFile)
    val prefixes = Prefixes.fromXML(sieveConfig \ "Prefixes" head) ++ Prefixes.stdPrefixes
    val qualityProperties = (sieveConfig \ "QualityAssessment" \ "AssessmentMetric").map( n => prefixes.resolve((n \ "@id").text ))
    val fusionProperties = (sieveConfig \\ "Fusion" \\ "Class" \\ "Property").map(n => prefixes.resolve((n \ "@name").text ))
    val r = (qualityProperties ++ fusionProperties)
    log.trace("Used properties: %s".format(r.toString()))
    val set = new HashSet[String]
    set ++= r
    set
  }

  def main(args: Array[String]) {

    SieveConfig.getUsedProperties(new File("ldif/examples/rio/sieve/rio-config.xml"))
  }
}
