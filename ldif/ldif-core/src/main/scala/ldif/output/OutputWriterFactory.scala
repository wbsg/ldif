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

package ldif.output

import java.util.Properties
import org.slf4j.LoggerFactory

object OutputWriterFactory {

  private val log = LoggerFactory.getLogger(getClass.getName)

  def getWriter(key : String, properties : Properties) : OutputWriter = {

    if (key == "sparql") {
      val sparqlEndpointURI = properties.getProperty("sparqlEndpointURI", "")
      val sparqlEndpointUsr = properties.getProperty("sparqlEndpointUsr", "")
      val sparqlEndpointPwd = properties.getProperty("sparqlEndpointPwd", "")
      // TODO check parameters
      SparqlWriter(sparqlEndpointURI, Some(sparqlEndpointUsr, sparqlEndpointPwd))
    }

    else if (key == "nq" || key == "nt") {
      val filePath = "output"  // TODO
      new FileWriter(filePath, key)
    }

    else {
      log.warn("Output writer not supported")
      // TODO
      null
    }

  }

}