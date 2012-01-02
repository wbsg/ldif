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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import ldif.entity.Restriction.{Condition, And}
import ldif.entity._
import org.scalatest.matchers.ShouldMatchers

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 06.07.11
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class EntityDescriptionToSparqlConverterTest extends FlatSpec with ShouldMatchers {

  it should "convert an EntityDescription to a SPARQL query" in {
    val ed = EntityDescription(Restriction(
                                Some(
                                  And(
                                    List(
                                      Condition(
                                        Path("?SUBJ", List(ForwardOperator(new Uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))),
                                        Set(Node.createUriNode("Class", ""))),
                                      Condition(
                                        Path("?SUBJ", List(ForwardOperator(new Uri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))),
                                        Set(Node.createUriNode("Another_Class", "")))
                                )))),
                                Vector(Vector(
                                  Path("SUBJ", List(ForwardOperator(new Uri("p1")), ForwardOperator(new Uri("p2")))),
                                  Path("SUBJ", List(ForwardOperator(new Uri("p1")), ForwardOperator(new Uri("p3")))))))
    EntityDescriptionToSparqlConverter.convert(ed).head.toString should equal ("(SELECT " +EntityDescriptionToSparqlConverter.afterSelect + "?ldifvar1 ?ldifvar1graph ?ldifvar2 ?ldifvar2graph ?SUBJ ?ldifph1 ?ldifph2 ?ldifph4 ?ldifph6  {  { GRAPH ?ldifph1 { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Class> .  } GRAPH ?ldifph2 { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Another_Class> .  }  } GRAPH ?ldifph4 { ?SUBJ <p1> ?ldifph3 .  } GRAPH ?ldifvar1graph { ?ldifph3 <p2> ?ldifvar1 .  } GRAPH ?ldifph6 { ?SUBJ <p1> ?ldifph5 .  } GRAPH ?ldifvar2graph { ?ldifph5 <p3> ?ldifvar2 .  }  } ORDER BY ?SUBJ,ArrayBuffer(ldifph1, ldifph2, ldifph4, ldifph6))")

    // use default graph
    EntityDescriptionToSparqlConverter.convert(ed, true).head.toString should equal ("(SELECT " +EntityDescriptionToSparqlConverter.afterSelect + "?ldifvar1 ?ldifvar2 ?SUBJ  WHERE  {  { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Class> . ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Another_Class> .  } ?SUBJ <p1> ?ldifph3 . ?ldifph3 <p2> ?ldifvar1 . ?SUBJ <p1> ?ldifph5 . ?ldifph5 <p3> ?ldifvar2 .  } ORDER BY ?SUBJ,ArrayBuffer(ldifph1, ldifph2, ldifph4, ldifph6))")

    // use specific named graph
    EntityDescriptionToSparqlConverter.convert(ed, "http://myGraph" ).head.toString should equal ("(SELECT " +EntityDescriptionToSparqlConverter.afterSelect + "?ldifvar1 ?ldifvar2 ?SUBJ  FROM <http://myGraph>  WHERE  {  { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Class> . ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Another_Class> .  } ?SUBJ <p1> ?ldifph3 . ?ldifph3 <p2> ?ldifvar1 . ?SUBJ <p1> ?ldifph5 . ?ldifph5 <p3> ?ldifvar2 .  } ORDER BY ?SUBJ,ArrayBuffer(ldifph1, ldifph2, ldifph4, ldifph6))")



  }

}