package ldif.util

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

import org.slf4j.LoggerFactory
import java.util.Properties
import java.io.{IOException, FileInputStream, BufferedInputStream, File}

object ConfigProperties {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def loadProperties(propertiesFile : String) : Properties = {
    loadProperties(new File(propertiesFile))
  }

  def loadProperties(propertiesFile : File): Properties = {

    val properties = new Properties
    if(propertiesFile.exists) {

      try {
        val stream = new BufferedInputStream(new FileInputStream(propertiesFile))
        properties.load(stream)
        stream.close
        // add properties file path as propertiesFile value
        properties.setProperty("propertiesFile", propertiesFile.getCanonicalPath)

      } catch {
        case e: IOException => {
          log.error("Error reading the properties file at: " + propertiesFile.getCanonicalPath)
        }
      }
    }
    else
      log.warn("No properties file found at: " + propertiesFile.getCanonicalPath)

    properties
  }

}