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

package ldif.output

import java.util.Properties
import org.slf4j.LoggerFactory
import ldif.util.Consts
import ldif.runtime.QuadWriter

object OutputWriterFactory {

  private val log = LoggerFactory.getLogger(getClass.getName)

  // TODO add outputFormat to integration config
  def getWriter(properties : Properties, outputUriOrPath : String = null) : QuadWriter = {

    val outputFormat =  properties.getProperty("outputFormat", "nq").toLowerCase

    if (outputFormat == "sparql") {
      val sparqlEndpointURI = properties.getProperty("sparqlEndpointURI", "")
      if (sparqlEndpointURI == "") {
        log.warn("SPARQL Endpoint URI not defined. Please use the property 'sparqlEndpointURI'")
        null
      }
      else {
        val sparqlEndpointUsr = properties.getProperty("sparqlEndpointUsr", "")
        val sparqlEndpointPwd = properties.getProperty("sparqlEndpointPwd", "")
        val sparqlUpdateVersion = properties.getProperty("sparqlUpdateVersion", Consts.SparqlUpdateDefaultVersion)
        val sparqlEndpointParam = properties.getProperty("sparqlEndpointParam", Consts.SparqlDefaultParameter)
        SparqlWriter(sparqlEndpointURI, Some(sparqlEndpointUsr, sparqlEndpointPwd), sparqlUpdateVersion, sparqlEndpointParam )
      }
    }

    else if (outputFormat == "nq" || outputFormat == "nt") {
      new FileWriter(outputUriOrPath, outputFormat)
    }

    else {
      log.warn("Output format not supported: "+ outputFormat )
      null
    }

  }

}