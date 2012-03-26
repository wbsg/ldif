/*
 * LDIF
 *
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

package ldif.config

import java.io.File
import org.slf4j.LoggerFactory
import xml.{Node, XML}
import java.util.Properties
import java.lang.Boolean
import ldif.util.{ValidatingXMLReader, ConfigProperties, Consts}

case class IntegrationConfig (sources : Traversable[String],
                              linkSpecDir : File,
                              mappingDir : File,
                              sieveSpecDir : File,
                              outputs : OutputConfig,
                              properties : Properties,
                              runSchedule : String)

object IntegrationConfig {

  protected val log = LoggerFactory.getLogger(getClass.getName)

  protected val schemaLocation = "xsd/IntegrationJob.xsd"

  protected var baseDir : String = null
  protected var xml : Node = null
  protected var properties : Properties = new Properties

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File)  : IntegrationConfig = {
    baseDir = configFile.getParent
    xml = XML.loadFile(configFile)

    // Read in properties
    val propertiesFile = getFile("properties", baseDir)
    properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    IntegrationConfig(
      getSources,
      getLinkSpecDir,
      getMappingDir,
      getSieveSpecDir,
      getOutput,
      properties,
      getRunSchedule
    )
  }

  protected def getSources = SourceConfig.fromXML((xml \ "sources").head, baseDir)
  protected def getLinkSpecDir = getFile("linkSpecifications", baseDir, true)
  protected def getMappingDir = getFile("mappings", baseDir, true)
  protected def getSieveSpecDir = getFile("sieve", baseDir, true)
  protected def getOutput = {
    if((xml \ "outputs").length>0)
      OutputConfig.fromOutputsXML((xml \ "outputs").head)
    else
      OutputConfig.fromOutputXML((xml \ "output").head)
  }
  protected def getRunSchedule = getString("runSchedule", "onStartup")

  protected def getString(key : String, default : String = null) = {
    var value : String = (xml \ key text)
    if (value == "" || value == null)
      value = default
    value
  }

  /**
   * Returns a File object if the element is defined and the value is not empty, else it returns null
   */
  protected def getFile(key : String, baseDir : String, skipIfNotDefined : Boolean = false) : File = {
    val value : String = (xml \ key text)
    var file : File = null
    if (value != ""){
      val relativeFile = new File(baseDir + Consts.fileSeparator + value)
      val absoluteFile = new File(value)
      if (relativeFile.exists || absoluteFile.exists) {
        if (relativeFile.exists)
          file = relativeFile
        else file = absoluteFile
      }
      else {
        log.warn("\'"+key+"\' path not found. Searched: " + relativeFile.getCanonicalPath + ", " + absoluteFile.getCanonicalPath)
      }
    }
    else{
      if(skipIfNotDefined && properties != null)
        properties.setProperty(key + ".skip", "true")
      log.warn("\'"+key+"\' is not defined in the IntegrationJob config")
    }
    file
  }

}
