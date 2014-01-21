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

package ldif.hadoop

import ldif.entity.EntityDescription
import ldif.util.ConfigProperties
import java.io.File
import java.util.Properties
import runtime.ConfigParameters
import ldif.hadoop.entitybuilder.HadoopEntityBuilder
import org.apache.hadoop.fs.Path

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/26/12
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */

object EB {
  def buildEntities(input: String, output: String, entityDescriptions: IndexedSeq[EntityDescription]) {
    val properties = {
      if(System.getProperty("ldif.properties", "")!="") {
        println("Using properties file: " + System.getProperty("ldif.properties"))
        ConfigProperties.loadProperties(System.getProperty("ldif.properties"))
      }
      else if(new File("ldif.properties").exists()) {
        println("Using properties file: ldif.properties from working directory")
        ConfigProperties.loadProperties("ldif.properties")
      }
      else {
        println("No ldif.properties file found.")
        new Properties()
      }
    }
    val configParameters = ConfigParameters(new Properties(), null, null, null, true)
    val eb = new HadoopEntityBuilder(entityDescriptions, Seq(new Path(input)), configParameters)
    eb.buildEntities(new Path(output))
  }
}