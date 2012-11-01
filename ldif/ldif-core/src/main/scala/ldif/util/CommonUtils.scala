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

package ldif.util

import ldif.datasources.dump.QuadParser
import ldif.runtime.Quad
import xml.Node
import org.slf4j.LoggerFactory
import java.util.{Calendar, Date, Properties}
import java.net.URL
import io.Source
import java.io.{FileWriter, File}

object CommonUtils {

  private val log = LoggerFactory.getLogger(getClass.getName)

  var currentDir : String = null

  // convert a Map[String,String] to a Properties object
  def buildProperties(customProperties : Map[String,String]) = {
    // More details - http://ldif.wbsg.de/#integrationProperties
    val properties = new Properties()
    if (customProperties.size > 0)
      for((key,value) <- customProperties)
        properties.setProperty(key,value)
    properties
  }

  // convert a Traversable[String] to a Traversable[Quad]
  def getQuads(lines : Traversable[String]): Traversable[Quad] = {
    val parser = new QuadParser
    lines.map(parser.parseLine(_))
  }

  // create a Traversable[Quad] from a file
  def getQuads(file : File) : Traversable[Quad] = {
    getQuads(scala.io.Source.fromFile(file).getLines.toTraversable)
  }

  def getValueAsString(xml : Node, key : String, default : String = "") = {
    var value : String = (xml \ key text)
    if (value == "" || value == null)
      value = default
    value
  }

  def getAttributeAsString(xml: Node, name: String, default: String="") = {
    val attributeRef = "@"+name
    var value: String = (xml \ attributeRef text)
    if (value == "" || value == null)
      value = default
    value
  }

  // list files contained in the directory 'dir', filtering hidden files or by extension
  def listFiles(dir : File, allowedExtensions : Seq[String] = Seq.empty[String], keepHidden : Boolean = false) : Traversable[File] = {
    if (dir.isDirectory){
      var files = dir.listFiles
      if(keepHidden) files = files.filterNot(_.isHidden)
      for(ext <- allowedExtensions) files = files.filter(_.getName.endsWith("."+ext))
      files.toTraversable
    }
    else {
      log.warn(dir.getCanonicalPath +" is not a Directory.")
      Traversable.empty[File]
    }
  }

  // Check if the given file has a valid extension
  def isValidFile(file : File, allowedExtensions : Seq[String])  : Boolean = {
    if(allowedExtensions.size == 0)
      return true
    for(ext <- allowedExtensions)
      if(file.getName.endsWith("."+ext))
        return true
    false
  }

  // helper method
  def listFiles(dir : File, allowedExtension : String) : Traversable[File] = {
    listFiles(dir,Seq(allowedExtension))
  }

  // Convert Date to a Calendar
  def dateToCalendar(date : Date) : Calendar = {
    val cal = Calendar.getInstance
    cal.setTime(date)
    cal
  }

  // Get a file from a given location (local path or url)
  def getFileFromPathOrUrl(location : String, baseDir : String = currentDir) : File = {
    if (location.equals("") || location == null)
      null
    else if (!isLocal(location))
      getFileFromUrl(location)
    else
      getFileFromPath(location, baseDir)
  }

  // Get a file from a given URL (as String)
  def getFileFromUrl(url : String) : File =
    getFileFromUrl(new URL(url))

  // Get a file from a given URL
  def getFileFromUrl(url : URL) : File =  {
    val urlString = url.toString
    log.info("Loading " + urlString)
    var file =  TemporaryFileCreator.createTemporaryFile("ldif", urlString.substring(urlString.lastIndexOf('/')+1), false)
    val writer = new FileWriter(file.getCanonicalPath)
    try {
      val lines = Source.fromURL(url).getLines()
      for (line <- lines)
        writer.write(line+"\n")
    } catch {
      case e:Exception => {
        log.warn(urlString + " did not provide any data")
        file = null
        throw e
      }
    }
    writer.flush()
    writer.close()
    file
  }

  // Get a file from a given path and base directory (opt)
  def getFileFromPath(filepath : String, baseDir : String = currentDir, suppressWarning : Boolean = false) : File = {
    // try as Resource, relative path and absolute path
    val resource = getClass.getClassLoader.getResource(filepath)
    val relativeFile = new File(baseDir + Consts.fileSeparator  + filepath)
    val absoluteFile = new File(filepath)
    if (resource!=null)
      new File(resource.toString.stripPrefix("file:"))
    else if (relativeFile.exists)
      relativeFile
    else {
      if (!suppressWarning & !absoluteFile.exists)
        log.warn("File not found. Searched: "+ filepath +", "+ relativeFile.getCanonicalPath + ", " + absoluteFile.getCanonicalPath)
      absoluteFile
    }
  }

  // Get an existing file path (if possible), return the value otherwise
  def getFilePath(value : String) : String = {
    val file = getFileFromPath(value, suppressWarning = true)
    if (file.exists())
      file.getCanonicalPath
    else value   // used as HDFS path
  }

  // Check if filepath is a local or remote path
  def isLocal(filepath : String) : Boolean = {
    var isLocal = true
    var url:URL = null
    try {
      url = new URL(filepath)
      isLocal = false
    } catch {
      case e:Exception => {}
    }

    if (url != null && url.getProtocol.toLowerCase.equals("file")) {
      isLocal = true
    }

    isLocal
  }

  // Search for a writable path for the given filepath. First try the relative path, then the absolute one.
  def getWritablePath(filepath : String, baseDir : String = currentDir) : Option[String] = {
    val relativeFile = new File(baseDir + Consts.fileSeparator  + filepath).getCanonicalFile
    val absoluteFile = new File(filepath).getCanonicalFile

    if (baseDir!=null & isWritable(relativeFile))
      Some(relativeFile.getCanonicalPath)
    else if (isWritable(absoluteFile))
      Some(absoluteFile.getCanonicalPath)
    else {
      log.warn("Unable to find a writable path for "+filepath+". Searched: " + relativeFile.getCanonicalPath + ", " + absoluteFile.getCanonicalPath)
      None
    }
  }

  // Check if the given file is writable
  def isWritable(file : File) : Boolean = {
    if (!file.exists) {
      val dir = file.getParentFile

      if(dir.exists || dir.mkdirs()) {
        try {
          file.createNewFile()
          return true
        }
        catch { case e:Exception => log.debug("Unable to write to: " + file.getCanonicalPath)}
      }
    }
    else if (file.canWrite) {
      return true
    }
    false
  }

}

