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


import org.slf4j.LoggerFactory
import java.io.File
import xml.{Node, XML}
import java.util.Properties
import ldif.util.{ConfigProperties, CommonUtils, ValidatingXMLReaderWithBool, Consts}

case class SchedulerConfig (importJobsFiles : Traversable[File], integrationJob : File, dataSourcesFiles : Traversable[File], dumpLocationDir : String, properties : Properties)  {

  def isAnyImportJobDefined = importJobsFiles.size > 0
  def isAnyDataSourceDefined = dataSourcesFiles.size > 0
  def isAnyPropertyDefined = properties.isEmpty

  def toXML : xml.Node = {
    <scheduler xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www4.wiwiss.fu-berlin.de/ldif/ ../../xsd/SchedulerConfig.xsd"
                     xmlns="http://www4.wiwiss.fu-berlin.de/ldif/">
      {if (isAnyDataSourceDefined){
          <dataSources>
            {for (dataSource <- dataSourcesFiles) yield { <dataSource>{dataSource.getCanonicalPath}</dataSource> } }
          </dataSources>}
      }
      {if (isAnyImportJobDefined){
          <importJobs>
            {for (importJob <- importJobsFiles) yield { <importJob>{importJob.getCanonicalPath}</importJob> } }
          </importJobs>}
      }
      {if (isAnyPropertyDefined) <properties>{properties.getProperty("propertiesFile")}</properties>}
      <integrationJob>{integrationJob.getCanonicalPath}</integrationJob>
      <dumpLocation>{dumpLocationDir}</dumpLocation>
    </scheduler>}
}

object SchedulerConfig
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  private val schemaLocation = "xsd/SchedulerConfig.xsd"

  def empty = SchedulerConfig(null, null, null, null, new Properties)

  def load = new ValidatingXMLReaderWithBool(fromFile, schemaLocation)

  def fromFile(file : File, useHdfsPaths : Boolean = false)  : SchedulerConfig =
    fromXML(XML.loadFile(file), file.getCanonicalFile.getParent, useHdfsPaths)

  def fromString(xmlString : String, dir : String) =
    fromXML(XML.loadString(xmlString), dir)

  def fromXML(xml : Node, dir : String, useHdfsPaths : Boolean = false) : SchedulerConfig = {
    CommonUtils.currentDir = dir

    // Read in properties
    val propertiesFile = getFile(xml, "properties")
    var properties = new Properties
    if (propertiesFile != null)
      properties = ConfigProperties.loadProperties(propertiesFile)

    // dumpLocation can be (1) a relative local path (from baseDir),
    // (2) an absolute local path or (3) an HDFS path,
    val dumpLocation = (xml \ "dumpLocation" text)
    val dumpLocationDir =
      if (useHdfsPaths)
        dumpLocation
      else
        CommonUtils.getDirPath(dumpLocation).getOrElse({
          log.error("Error in the Scheduler configuraion: dumpLocation is not writable")
          sys.exit(1)
        })
    val importJobsFiles = getFiles(xml, "importJob", Seq("xml"))
    val integrationJobDir = getFile(xml, "integrationJob")
    val dataSourceFiles = getFiles(xml, "dataSources", Seq("xml"))

    SchedulerConfig(
      importJobsFiles,
      integrationJobDir,
      dataSourceFiles,
      dumpLocationDir,
      properties
    )
  }

  // Get a single file from a given xml element
  private def getFile(xml : Node, key : String, allowedExtensions : Seq[String] = Seq.empty) : File = {
    val files = getFiles(xml, key, allowedExtensions)
      if (files.size > 0){
        if (files.size > 1)
          log.warn("More than one file found for "+key)
        files.head
      }
    else null
  }

  // Get files from a given xml element
  private def getFiles(xml : Node, key : String, allowedExtensions : Seq[String] = Seq.empty, forceMkdir : Boolean = false) : Traversable[File] = {
    var files = Traversable.empty[File]
    val nodes = (xml \\ key)
    for (node <- nodes.filter(_.text != ""))
        files = files ++ getFiles(node.text, allowedExtensions, forceMkdir)
    files
  }

  // Get valid files from a given path (local path or url)
  private def getFiles(location : String, allowedExtensions : Seq[String], forceMkdir : Boolean) : Traversable[File] = {
    val file = getFile(location, forceMkdir)
    var files = Traversable.empty[File]
    if (file!= null)  {
      if (file.isDirectory)
        files = CommonUtils.listFiles(file, allowedExtensions)
      else if(CommonUtils.isValidFile(file, allowedExtensions))
        files = Traversable(file)
    }
    files
  }

  // Get a file from a given path (local or url)
  private def getFile(filepath : String, forceMkdir : Boolean) : File = {
      var file = CommonUtils.getFileFromPathOrUrl(filepath)
      if(file == null)
        if (forceMkdir) {
          val relativeFile = new File(CommonUtils.currentDir + Consts.fileSeparator  + filepath)
          if (relativeFile.mkdirs) {
            file = relativeFile
            log.info("Created new directory at: "+ relativeFile.getCanonicalPath)
          }
          else log.error("Error creating directory at: " + relativeFile.getCanonicalPath)
        }
      file
  }
}

