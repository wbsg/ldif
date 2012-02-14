package ldif.util

import java.util.Properties
import java.io.File
import ldif.datasources.dump.QuadParser
import ldif.runtime.Quad
import xml.Node

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
object CommonUtils {

  // convert a Map[String,String] to a Properties object
  def buildProperties(customProperties : Map[String,String]) = {
    // More details - http://www4.wiwiss.fu-berlin.de/bizer/ldif/#configurationproperties
    val properties = new Properties()
    if (customProperties.size > 0)
      for((key,value) <- customProperties)
        properties.setProperty(key,value)
    properties
  }

  // load a file
  def loadFile(filePath : String) =  {
    val configUrl = getClass.getClassLoader.getResource(filePath)
    new File(configUrl.toString.stripPrefix("file:"))
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

}

