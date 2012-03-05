package scala.ldif.modules.sieve.fusion

import ldif.modules.sieve.fusion.functions.{PassItOn, KeepFirst}
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Restriction.Condition
import ldif.entity.{Node, Path, EntityDescription}
import ldif.util.Prefixes
import ldif.modules.sieve.fusion.FusionFunction

/**
 * Tests the creation of a fusion configuration from an xml configuration file
 *
 * @author Hannes Muehleisen
 */

class KeepFirstTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <FusionFunction class="KeepFirst" metric="sieve:recency"/>
  val fusionObj = new KeepFirst("sieve:recency");

  val prefixes = new Prefixes(Map(("rdfs", "http://example.com/rdfs"), ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), ("dbpedia", "http://example.com/dbpedia"), ("dbpedia-owl", "http://example.com/dbpedia-owl"),("sieve", "http://sieve.wbsg.de/vocab/")))

  val classNode = Node.createUriNode(prefixes.resolve("dbpedia:Settlement"))
  val condition = new Condition(Path.parse("?a/rdf:type")(prefixes), Set(classNode))


  it should "create the correct fusion configuration from XML" in {
    (KeepFirst.fromXML(fusionXml)(prefixes) equals fusionObj)
  }

}


