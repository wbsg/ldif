package ldif.modules.sieve.fusion

import functions.{KeepFirst, PassItOn}
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Restriction.Condition
import ldif.entity.{Node, Path, EntityDescription}
import ldif.util.Prefixes

/**
 * Tests the creation of a fusion configuration from an xml configuration file
 *
 * @author Hannes Muehleisen
 */

class FusionConfigTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <Fusion name="bla" description="blubb">
    <Class name="dbpedia:Settlement">
      <Property name="rdfs:label">
          <FusionFunction class="PassItOn"/>
      </Property>
      <Property name="dbpedia-owl:areaTotal">
          <FusionFunction class="KeepFirst"
                          metric="sieve:reputation"/>
      </Property>

    </Class>
  </Fusion>

  val prefixes = new Prefixes(Map(("rdfs", "http://example.com/rdfs"), ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), ("dbpedia", "http://example.com/dbpedia"), ("dbpedia-owl", "http://example.com/dbpedia-owl"),("sieve", "http://sieve.wbsg.de/vocab/")))


  val classNode = Node.createUriNode(prefixes.resolve("dbpedia:Settlement"))
  val condition = new Condition(Path.parse("?a/rdf:type")(prefixes), Set(classNode))

  val entityDescription = new EntityDescription(new ldif.entity.Restriction(Option(condition)), IndexedSeq(IndexedSeq(Path.parse("?a/rdfs:label")(prefixes)), IndexedSeq(Path.parse("?a/dbpedia-owl:areaTotal")(prefixes))))

  val mcFusion = new FusionConfig(IndexedSeq(new FusionSpecification("test",IndexedSeq(new PassItOn, new KeepFirst("sieve:reputation")), IndexedSeq(prefixes.resolve("rdfs:label"), prefixes.resolve("dbpedia-owl:areaTotal")))), IndexedSeq(entityDescription))
  val gFusion = FusionConfig.fromXML(fusionXml)(prefixes)

  it should "create the correct fusion configuratioon from XML" in {
    (gFusion.entityDescriptions) should equal(mcFusion.entityDescriptions)
    (gFusion.fusionSpecs) should equal(mcFusion.fusionSpecs)
    (gFusion) should equal(mcFusion)
  }

}


