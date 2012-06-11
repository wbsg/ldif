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
  val LDIF_VERSION = "0.6"
  val LDIF_WEBSITE = "http://ldif.wbsg.de"
  val LDIF_HELP_HEADER = ("LDIF "+LDIF_VERSION+" \nLicensed under Apache License v2.0")
  val LDIF_HELP_FOOTER = ("\nMore information at "+LDIF_WEBSITE)
  val LDIF_NS = "http://www4.wiwiss.fu-berlin.de/ldif/"

  val DEFAULT_GRAPH = LDIF_NS + "defaultGraph"
  val DEFAULT_PROVENANCE_GRAPH = LDIF_NS + "provenance"
  val DEFAULT_IMPORTED_GRAPH_PREFIX = LDIF_NS + "graph#"
  val SAMEAS_URI = "http://www.w3.org/2002/07/owl#sameAs"
  val RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  val RDFTYPE_URI = RDF_NS + "type"
  val RDFFIRST = RDF_NS + "first"
  val RDFREST = RDF_NS + "rest"

  val OWL_NS = "http://www.w3.org/2002/07/owl#"
  val OWL_CLASS = OWL_NS + "Class"
  val OWL_OBJECTPROPERTY = OWL_NS + "ObjectProperty"
  val OWL_DATATYPEPROPERTY = OWL_NS + "DatatypeProperty"
  val OWL_UNIONOF = OWL_NS + "unionOf"
  val RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#"
  val RDFS_SUBCLASSOF = RDFS_NS + "subClassOf"
  val RDFS_LABEL = RDFS_NS + "label"
  val RDFS_DOMAIN = RDFS_NS + "domain"
  val RDFS_RANGE = RDFS_NS + "range"

  val URI_MINTING_GRAPH = LDIF_NS + "graph#uriMinting"
  val URI_REWRITING_GRAPH = LDIF_NS + "graph#uriRewriting"
  val SILK_OUT_GRAPH = LDIF_NS + "graph#generatedBySilk"

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
  val xsdDouble = "http://www.w3.org/2001/XMLSchema#double"
  val xsdNonNegativeInteger = "http://www.w3.org/2001/XMLSchema#nonNegativeInteger"

  // Import metadata properties
  val lastUpdateProp = LDIF_NS + "lastUpdate"
  val hasImportJobProp = LDIF_NS + "hasImportJob"
  val importIdProp = LDIF_NS + "importId"
  val hasDatasourceProp = LDIF_NS + "hasDatasource"
  val hasImportTypeProp = LDIF_NS + "hasImportType"
  val hasOriginalLocationProp = LDIF_NS + "hasOriginalLocation"
  val numberOfQuadsProp = LDIF_NS + "numberOfQuads"
  val rdfTypeProp = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
  val importedGraphClass = LDIF_NS + "ImportedGraph"
  val importJobClass = LDIF_NS + "ImportJob"

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
  val SparqlTripleLimitDefault = Long.MaxValue

  val FileOutputFormatDefault = "nquads"
  val OutputPhaseDefault = "complete"

  // Status interface
  val DefaultStatusMonitorrURI = "http://localhost:5343/"

  // XSD locations
  val xsdBase = "http://www.assembla.com/code/ldif/git/nodes/ldif/ldif-core/src/main/resources/xsd/"
  val xsdIntegration = xsdBase + "IntegrationJob.xsd"
  val xsdScheduler = xsdBase + "SchedulerConfig.xsd"
  val xsdDataSource = xsdBase + "DataSource.xsd"
  val xsdImportJob = xsdBase + "ImportJob.xsd"

  // Matcher properties
  val LDIF_numberOfChildren = LDIF_NS + "nrOfChildren"
  val LDIF_hierarchyLevel = LDIF_NS + "hierarchyLevel"
  val LDIF_sizeOfSubTree = LDIF_NS + "sizeOfSubTree"
  val LDIF_setMember = LDIF_NS + "setMember"
}