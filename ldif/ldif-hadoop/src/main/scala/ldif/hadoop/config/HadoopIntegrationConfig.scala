/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.hadoop.config;

import java.io.File
import java.util.Properties
import org.slf4j.LoggerFactory
import ldif.util.ValidatingXMLReader
import xml.{Node, XML}

case class HadoopIntegrationConfig(sources : String, linkSpecDir : String, mappingDir : String, sieveSpecDir : String, outputFile : String,  properties : Properties, runSchedule : String) {}

object HadoopIntegrationConfig
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  private val schemaLocation = "xsd/IntegrationJob.xsd"

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File) =
  {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    var properties = new Properties
    //   val propertiesFile = getFile(xml, "properties", baseDir)
    //   if (propertiesFile != null)
    //     properties = ConfigProperties.loadProperties(propertiesFile)   //TODO property parser

    var runSchedule : String = (xml \ "runSchedule" text)
    if (runSchedule == "" || runSchedule == null)
      runSchedule = "onStartup"

    HadoopIntegrationConfig(      //TODO validation
      sources = (xml \ "sources" text),
      linkSpecDir = (xml \ "linkSpecifications" text),
      mappingDir  = (xml \ "mappings" text),
      sieveSpecDir = (xml \ "fusion" text),
      outputFile = (xml \ "output" text),
      properties = properties,
      runSchedule = runSchedule
    )
  }

}