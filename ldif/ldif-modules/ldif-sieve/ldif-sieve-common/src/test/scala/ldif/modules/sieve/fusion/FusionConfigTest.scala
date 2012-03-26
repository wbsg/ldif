package ldif.modules.sieve.fusion

import functions.{KeepFirst, PassItOn}
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.{Node, Path, EntityDescription}
import ldif.util.Prefixes
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.Restriction.{Or, Condition}
import collection.mutable.ArraySeq

/**
 * Tests the creation of a fusion configuration from an xml configuration file
 *
 * @author Hannes Muehleisen
 */
@RunWith(classOf[JUnitRunner])
class FusionConfigTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <Fusion name="bla" description="blubb">
    <Class name="dbpedia-owl:Settlement">
      <Property name="rdfs:label">
          <FusionFunction class="PassItOn"/>
      </Property>
      <Property name="dbpedia-owl:areaTotal">
          <FusionFunction class="KeepFirst"
                          metric="sieve:reputation"/>
      </Property>

    </Class>
  </Fusion>

  val twoClassesFusionXml = <Fusion name="bla" description="blubb">
    <Class name="dbpedia-owl:Settlement dbpedia-owl:City">
      <Property name="rdfs:label">
          <FusionFunction class="PassItOn"/>
      </Property>
      <Property name="dbpedia-owl:areaTotal">
          <FusionFunction class="KeepFirst"
                          metric="sieve:reputation"/>
      </Property>

    </Class>
  </Fusion>


  val prefixes = Prefixes.stdPrefixes ++ new Prefixes(Map("dbpedia-owl" -> "http://example.com/dbpedia-owl"))

  val settlementClassNode = Node.createUriNode(prefixes.resolve("dbpedia-owl:Settlement"))
  val cityClassNode = Node.createUriNode(prefixes.resolve("dbpedia-owl:City"))
  val condition = new Condition(Path.parse("?a/rdf:type")(prefixes), Set(settlementClassNode))
  val entityDescription = new EntityDescription(new ldif.entity.Restriction(Option(condition)), IndexedSeq(IndexedSeq(Path.parse("?a/rdfs:label")(prefixes)), IndexedSeq(Path.parse("?a/dbpedia-owl:areaTotal")(prefixes))))

  val mcFusion = new FusionConfig(IndexedSeq(new FusionSpecification("test",IndexedSeq(new PassItOn, new KeepFirst(prefixes.resolve("sieve:reputation"))), IndexedSeq(prefixes.resolve("rdfs:label"), prefixes.resolve("dbpedia-owl:areaTotal")))), IndexedSeq(entityDescription))
  val gFusion = FusionConfig.fromXML(fusionXml)(prefixes)

  val twoClassesCondition = Or(ArraySeq(new Condition(Path.parse("?a/rdf:type")(prefixes), Set(settlementClassNode)),
                               new Condition(Path.parse("?a/rdf:type")(prefixes), Set(cityClassNode))))
  val twoClassesEntityDescription = new EntityDescription(new ldif.entity.Restriction(Option(twoClassesCondition)), IndexedSeq(IndexedSeq(Path.parse("?a/rdfs:label")(prefixes)), IndexedSeq(Path.parse("?a/dbpedia-owl:areaTotal")(prefixes))))
  val twoClassesMcFusion = new FusionConfig(IndexedSeq(new FusionSpecification("test",IndexedSeq(new PassItOn, new KeepFirst(prefixes.resolve("sieve:reputation"))), IndexedSeq(prefixes.resolve("rdfs:label"), prefixes.resolve("dbpedia-owl:areaTotal")))), IndexedSeq(twoClassesEntityDescription))
  val twoClassesGFusion = FusionConfig.fromXML(twoClassesFusionXml)(prefixes)

  it should "create the correct fusion configuration from XML" in {
    (gFusion.entityDescriptions) should equal(mcFusion.entityDescriptions)
    (gFusion.fusionSpecs) should equal(mcFusion.fusionSpecs)
    (gFusion) should equal(mcFusion)
  }

  it should "create the correct fusion function when there are multiple classes" in {
    (twoClassesGFusion) should equal(twoClassesMcFusion)
  }

  it should  "cry out loud" in {
    (true equals false)
  }

}


