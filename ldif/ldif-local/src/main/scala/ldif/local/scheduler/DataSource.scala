/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local.scheduler

import ldif.util.ValidatingXMLReader
import java.io.File
import xml.{XML, Node}

case class DataSource(label : String, description : String = null) {
  def toXML = {
    <dataSource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www4.wiwiss.fu-berlin.de/ldif/ ../../xsd/DataSource.xsd"
                xmlns="http://www4.wiwiss.fu-berlin.de/ldif/">
      <label>{label}</label>
      <description>{description}</description>
    </dataSource>
  }
}

object DataSource{

  private val schemaLocation = "xsd/DataSource.xsd"

  def load = new ValidatingXMLReader(fromFile, schemaLocation)

  def fromFile(file : File) = {
    fromXML(XML.loadFile(file))
  }

  def fromString(xmlString : String) = {
    fromXML(XML.loadString(xmlString))
  }

  def fromXML (node : Node) : DataSource = {
    val label : String = (node \ "label" text)
    val description = (node \ "description" text)
    DataSource(label, description)
  }
}
