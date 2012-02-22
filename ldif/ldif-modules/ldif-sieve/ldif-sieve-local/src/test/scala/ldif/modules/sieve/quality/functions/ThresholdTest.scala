package ldif.modules.sieve.quality.functions

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node

@RunWith(classOf[JUnitRunner])
class ThresholdTest extends FlatSpec with ShouldMatchers {

  val imXml = <ScoringFunction class="Threshold">
      <Param name="threshold" value="42"/>
      <Input path="?GRAPH/provenance:whatever"/>
  </ScoringFunction>

  val tsFunc = new Threshold(42)
  val subject = Node.fromString("<subject>")
  val above = new Node("52", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val below = new Node("2", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val bogusValue = new Node("foo", "http://www.w3.org/2001/XMLSchema#bar", Node.TypedLiteral, "graphId")


  it should "return the correct implementation given XML" in {
    (Threshold.fromXML(imXml)) should equal(tsFunc)
  }

  it should "correctly score input values" in {
    (tsFunc.score(subject, Traversable(IndexedSeq((above)))) should equal(1.0))
    (tsFunc.score(subject, Traversable(IndexedSeq((below)))) should equal(0.0))
  }

  it should "survive an invalid input" in {
    (tsFunc.score(subject, Traversable(IndexedSeq((bogusValue))))) should equal(0.0)
  }

}