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

package ldif.hadoop.config

import org.slf4j.LoggerFactory
import java.io.File
import xml.{Node, XML}
import java.util.Properties
import ldif.util.{ConfigProperties, ValidatingXMLReader}

case class HadoopSchedulerConfig (importJobsDir : File, integrationJob : File, dataSourcesDir : File, dumpLocationDir : String, properties : Properties)  {}

object HadoopSchedulerConfig
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  private val schemaLocation = "xsd/SchedulerConfig.xsd"

  def empty = HadoopSchedulerConfig(null, null, null, null, new Properties)

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(configFile : File) = {
    val baseDir = configFile.getParent
    val xml = XML.loadFile(configFile)

    // Read in properties
    val propertiesFile = getFile(xml, "properties", baseDir)
    var properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    val dumpLocationDir = (xml \ "dumpLocation" text)
    // TODO validate, mkdir

    val importJobsDir = getFile(xml, "importJobs", baseDir)
    val integrationJobDir = getFile(xml, "integrationJob", baseDir)
    val datasourceJobDir = getFile(xml, "dataSources", baseDir)

    HadoopSchedulerConfig(
      importJobsDir,
      integrationJobDir,
      datasourceJobDir,
      dumpLocationDir,
      properties
    )
  }

  private def getFile (xml : Node, key : String, baseDir : String, forceMkdir : Boolean = false) : File = {
    val value : String = (xml \ key text)
    var file : File = null
    if (value != ""){
      val relativeFile = new File(baseDir + "/" + value)
      val absoluteFile = new File(value)
      if (relativeFile.exists || absoluteFile.exists) {
        if (relativeFile.exists)
          file = relativeFile
        else file = absoluteFile
      }
      else {
        log.warn("\'"+key+"\' path not found. Searched: " + relativeFile.getCanonicalPath + ", " + absoluteFile.getCanonicalPath)
        if (forceMkdir) {
          if (relativeFile.mkdirs) {
            file = relativeFile
            log.info("Created new directory at: "+ relativeFile.getCanonicalPath)
          }
          else log.error("Error creating directory at: " + relativeFile.getCanonicalPath)
        }
      }
    }
    else{
      log.warn("\'"+key+"\' is not defined in the Scheduler config")
    }
    file
  }

}
