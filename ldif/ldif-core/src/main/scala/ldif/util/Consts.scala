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

import java.text.SimpleDateFormat
import collection.immutable.HashMap

object Consts {
  val DEFAULT_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/defaultGraph"
  val DEFAULT_PROVENANCE_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/provenance"
  val DEFAULT_IMPORTED_GRAPH_PREFIX = "http://www4.wiwiss.fu-berlin.de/ldif/graph#"
  val SAMEAS_URI = "http://www.w3.org/2002/07/owl#sameAs"
  val RDFTYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

  val URI_MINTING_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/graph#uriMinting"
  val URI_REWRITING_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/graph#uriRewriting"
  val SILK_OUT_GRAPH = "http://www4.wiwiss.fu-berlin.de/ldif/graph#generatedBySilk"

  val MAX_WAITING_TIME : Long = Long.MaxValue

  // String pool - for canonicalization
  val POOL_STARTING_SIZE : Int = 200
  val POOL_MAX_SIZE : Int = Int.MaxValue
  val DEFAULT_QUAD_QUEUE_CAPACITY: Int = 1000

  val DEFAULT_ENTITY_QUEUE_CAPACITY : Int = 100

  val changeFreqToHours = HashMap(
                "always" -> 0,
                "hourly" -> 1,
                "daily" -> 24,
                "weekly" -> 24 * 7,
                "monthly" -> 24 * 30,
                "yearly" -> 24 * 365,
                "onStartup" ->  Int.MaxValue)

  val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss.S")
  val xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

  // Import metadata properties
  val lastUpdateProp = "http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"
  val hasImportJobProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob"
  val importIdProp = "http://www4.wiwiss.fu-berlin.de/ldif/importId"
  val hasDatasourceProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource"
  val hasImportTypeProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasImportType"
  val hasOriginalLocationProp = "http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation"
  val rdfTypeProp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
  val importedGraphClass = "http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph"
  val importJobClass = "http://www4.wiwiss.fu-berlin.de/ldif/ImportJob"

  val MAX_NUM_GRAPHS_IN_MEMORY = 100000

  // SPARQL endpoint paramenter
  val maxPageSize = 5000
  val retryPause = 5 * 1000
  val retryCount = 5

  // System
  val fileSeparator = System.getProperty("file.separator")
  val tmpDir = System.getProperty("java.io.tmpdir")


  // Sparql
  val SparqlQueryParameterDefault = "udapte"
  val SparqlUpdateVersionDefault = "1.1"
  val SparqlMaxStatmentsPerRequest = 200
  val SparqlUseDirectPostDefault = "true"

  val FileOutputFormatDefault = "nq"
  val OutputPhaseDefault = "complete"
}