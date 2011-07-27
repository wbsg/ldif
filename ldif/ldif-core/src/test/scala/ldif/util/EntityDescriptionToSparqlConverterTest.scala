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
    EntityDescriptionToSparqlConverter.convert(ed).head.toString should equal ("(SELECT DISTINCT ?ldifvar1 ?ldifvar1graph ?ldifvar2 ?ldifvar2graph ?SUBJ ?ldifph1 ?ldifph2 ?ldifph4 ?ldifph6  {  { GRAPH ?ldifph1 { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Class> .  } GRAPH ?ldifph2 { ?SUBJ <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <Another_Class> .  }  } GRAPH ?ldifph4 { ?SUBJ <p1> ?ldifph3 .  } GRAPH ?ldifvar1graph { ?ldifph3 <p2> ?ldifvar1 .  } GRAPH ?ldifph6 { ?SUBJ <p1> ?ldifph5 .  } GRAPH ?ldifvar2graph { ?ldifph5 <p3> ?ldifvar2 .  }  } ORDER BY ?SUBJ,ArrayBuffer(ldifph1, ldifph2, ldifph4, ldifph6))")
  }

}