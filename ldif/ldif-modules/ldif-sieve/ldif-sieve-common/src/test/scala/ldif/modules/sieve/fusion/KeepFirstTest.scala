package scala.ldif.modules.sieve.fusion

import ldif.modules.sieve.fusion.functions.KeepFirst
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Restriction.Condition
import ldif.util.Prefixes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.modules.sieve.quality.HashBasedQualityAssessment
import ldif.entity.{NodeTrait, Node, Path}

/**
 * Tests the creation of a fusion function from an xml configuration file
 */
@RunWith(classOf[JUnitRunner])
class KeepFirstTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <FusionFunction class="KeepFirst" metric="sieve:recency"/>
  val fusionObj = new KeepFirst("sieve:recency");

  val prefixes = new Prefixes(Map(("rdfs", "http://example.com/rdfs"), ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), ("dbpedia", "http://example.com/dbpedia"), ("dbpedia-owl", "http://example.com/dbpedia-owl"),("sieve", "http://sieve.wbsg.de/vocab/")))

  val classNode = Node.createUriNode(prefixes.resolve("dbpedia:Settlement"))
  val condition = new Condition(Path.parse("?a/rdf:type")(prefixes), Set(classNode))

  /*
     Create three example nodes
   */
  val correctNode = new Node("pickMe!", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph3")
  val lowestNode = new Node("node1", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph1")
  val nodes = IndexedSeq(lowestNode,
                         new Node("node2", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph2"),
                         correctNode)
  val patterns =  Traversable[IndexedSeq[NodeTrait]](nodes)

  /**
   * Create three dummy values for property p1
   */
  val quality = new HashBasedQualityAssessment()
  quality.putScore("p1","graph1",0.1)
  quality.putScore("p1","graph2",0.2)
  quality.putScore("p1","graph3",0.3)

  it should "create the correct fusion function from XML" in {
    (KeepFirst.fromXML(fusionXml)(prefixes) equals fusionObj)
  }

  it should "keep the highest scored value from a list of quality assessed values" in {
    (fusionObj.fuse(patterns,quality) equals lowestNode)
  }

}


