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

package ldif.local.config

import java.util.logging.Logger
import java.util.Properties
import java.io.{IOException, FileInputStream, BufferedInputStream, File}

object ConfigProperties {
  private val log = Logger.getLogger(getClass.getName)

  def loadProperties(propertyPath : String) : Properties = {
    loadProperties(new File(propertyPath))
  }

  def loadProperties(propertyFile : File): Properties = {

    val properties = new Properties
    if(propertyFile.exists) {

      try {
        val stream = new BufferedInputStream(new FileInputStream(propertyFile))
        properties.load(stream)
        stream.close

      } catch {
        case e: IOException => {
          log.severe("Error reading the properties file at: " + propertyFile.getCanonicalPath)
        }
      }
    }
    else
      log.severe("No properties file found at: " + propertyFile.getCanonicalPath)

    properties
  }

}