/*
 * LDIF
 *
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

package ldif.config

import java.io.File
import org.slf4j.LoggerFactory
import xml.{Node, XML}
import java.util.Properties
import java.lang.Boolean
import ldif.util.{CommonUtils, ValidatingXMLReader, ConfigProperties}

case class IntegrationConfig (sources : Traversable[String],
                              linkSpecDir : File,
                              mappingDir : File,
                              sieveSpecDir : File,
                              outputs : OutputConfig,
                              properties : Properties,
                              runSchedule : String)   {

  def hasValidOutputs = outputs.validOutputs.size > 0

  def isAnySourceDefined = sources.size > 0
  def isAnyPropertyDefined = properties.isEmpty
  def isLinkSpecDefined = linkSpecDir != null
  def isMappingDefined = mappingDir != null
  def isSieveSpecDefined = sieveSpecDir != null
  def isAnyOutputDefined = outputs.outputs.size > 0

  //TODO implement outputConfig.toXML
  def toXML : xml.Node = {
    <integrationJob xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www4.wiwiss.fu-berlin.de/ldif/ ../../xsd/IntegrationJob.xsd"
                     xmlns="http://www4.wiwiss.fu-berlin.de/ldif/">
      {if (isAnySourceDefined){
        <sources>
          {for (source <- sources) yield { <source>{source}</source> } }
        </sources>}
      }
      {if (isMappingDefined) <mappings>{mappingDir}</mappings>}
      {if (isLinkSpecDefined) <linkSpecifications>{linkSpecDir}</linkSpecifications>}
      {if (isSieveSpecDefined) <sieve>{sieveSpecDir}</sieve>}
      {if (isAnyOutputDefined){
      <outputs>
        {for (output <- outputs.outputs) yield { <output><file>todo</file></output> } }
      </outputs>}
      }
      {if (isAnyPropertyDefined) <properties>{properties.getProperty("propertiesFile")}</properties>}
      <runSchedule>{runSchedule}</runSchedule>
    </integrationJob>}
}

object IntegrationConfig {

  protected val log = LoggerFactory.getLogger(getClass.getName)

  protected val schemaLocation = "xsd/IntegrationJob.xsd"

  protected var xml : Node = null
  protected var properties : Properties = new Properties

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(file : File)  : IntegrationConfig =
    fromXML(XML.loadFile(file), file.getCanonicalFile.getParent)

  def fromString(xmlString : String, dir : String) =
    fromXML(XML.loadString(xmlString), dir)

  def fromXML(node : Node, dir : String) : IntegrationConfig = {
    xml = node

    CommonUtils.currentDir = dir

    // Read in properties
    val propertiesFile = getFile("properties")
    properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    fromXML(node, properties, dir)
  }

  def fromXML(node : Node, props: Properties, dir : String) : IntegrationConfig = {
    xml = node

    CommonUtils.currentDir = dir

    properties = props

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

  protected def getSources = {
    if((xml \ "sources").length>0)
      SourceConfig.fromSourcesXML((xml \ "sources").head)
    else
      SourceConfig.fromSourceNode((xml \ "source").head)
  }
  protected def getLinkSpecDir = getFile("linkSpecifications", true)
  protected def getMappingDir = getFile("mappings", true)
  protected def getSieveSpecDir = getFile("sieve", true)
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
  protected def getFile(key : String, skipIfNotDefined : Boolean = false) : File = {
    val value : String = (xml \ key text)
    val file = CommonUtils.getFileFromPathOrUrl(value)
    if(file == null || !file.exists()){
      if (skipIfNotDefined && properties != null)
        properties.setProperty(key + ".skip", "true")
      log.warn("\'"+key+"\' is not defined (or invalid) in the IntegrationJob config")
    }
    file
  }
}
