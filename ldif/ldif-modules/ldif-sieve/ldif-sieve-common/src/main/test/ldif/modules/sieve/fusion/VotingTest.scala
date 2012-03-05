package scala.ldif.modules.sieve.fusion

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Restriction.Condition
import ldif.entity.{Node, Path}
import ldif.util.Prefixes
import ldif.modules.sieve.fusion.functions.Voting
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * Tests a fusion function
 */
@RunWith(classOf[JUnitRunner])
class VotingTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <FusionFunction class="Voting"/>
  val fusionObj = new Voting;

  val prefixes = new Prefixes(Map(("rdfs", "http://example.com/rdfs"), ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), ("dbpedia", "http://example.com/dbpedia"), ("dbpedia-owl", "http://example.com/dbpedia-owl"),("sieve", "http://sieve.wbsg.de/vocab/")))

  val classNode = Node.createUriNode(prefixes.resolve("dbpedia:Settlement"))
  val condition = new Condition(Path.parse("?a/rdf:type")(prefixes), Set(classNode))


  it should "create the correct fusion function from XML" in {
    (Voting.fromXML(fusionXml)(prefixes) equals fusionObj)
  }

}


