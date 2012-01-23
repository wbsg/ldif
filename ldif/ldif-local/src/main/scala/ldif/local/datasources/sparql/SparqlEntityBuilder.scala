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

package ldif.local.datasources.sparql

import ldif.util.EntityDescriptionToSparqlConverter
import ldif.local.runtime.EntityWriter
import ldif.entity.EntityDescription
import com.hp.hpl.jena.query.{ARQ, QueryExecutionFactory, QueryFactory}
import ldif.local.util.JenaResultSetEntityBuilderHelper

class SparqlEntityBuilder(endpointUrl : String) {

  // Build entities and write those into the EntityWriter
  def buildEntities (ed : EntityDescription, writer : EntityWriter) {
    // workaround - see http://stackoverflow.com/questions/5581769/sparql-xml-results-from-dbpedia-and-jena
    ARQ.getContext().setTrue(ARQ.useSAX)

    val queriesString = EntityDescriptionToSparqlConverter.convert(ed,true)
    val queries = for(queryString <- queriesString) yield  QueryFactory.create(queryString._1)

    val resultSets = for(query <- queries) yield QueryExecutionFactory.sparqlService(endpointUrl, query).execSelect
    JenaResultSetEntityBuilderHelper.buildEntitiesFromResultSet(resultSets, ed, writer, "<http://default>")
    //writer.finish
  }

}