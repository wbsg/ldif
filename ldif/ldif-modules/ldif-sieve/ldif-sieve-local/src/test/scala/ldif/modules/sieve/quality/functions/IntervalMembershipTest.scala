package ldif.modules.sieve.quality.functions

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node

@RunWith(classOf[JUnitRunner])
class IntervalMembershipTest extends FlatSpec with ShouldMatchers {

  val imXml = <ScoringFunction class="IntervalMembership">
      <Param name="from" value="6"/>
      <Param name="to" value="42"/>
      <Input path="?GRAPH/provenance:whatever"/>
  </ScoringFunction>

  val imFunc = new IntervalMembership(6, 42)
  val subject = Node.fromString("<subject>")
  val inValue = new Node("8", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val highOutValue = new Node("52", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val lowOutValue = new Node("2", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val lowerBorderValue = new Node("6", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")
  val upperBorderValue = new Node("42", "http://www.w3.org/2001/XMLSchema#int", Node.TypedLiteral, "graphId")

  val bogusValue = new Node("foo", "http://www.w3.org/2001/XMLSchema#bar", Node.TypedLiteral, "graphId")


  it should "return the correct implementation given XML" in {
    (IntervalMembership.fromXML(imXml)) should equal(imFunc)
  }

  it should "correctly score input values" in {
    (imFunc.score(subject, Traversable(IndexedSeq((inValue)))) should equal(1.0))
    (imFunc.score(subject, Traversable(IndexedSeq((lowerBorderValue)))) should equal(1.0))
    (imFunc.score(subject, Traversable(IndexedSeq((upperBorderValue)))) should equal(1.0))

    (imFunc.score(subject, Traversable(IndexedSeq((highOutValue)))) should equal(0.0))
    (imFunc.score(subject, Traversable(IndexedSeq((lowOutValue)))) should equal(0.0))
  }

  it should "only accept valid configuration" in {
    evaluating {
      new IntervalMembership(100, 10)
    } should produce[AssertionError]
    evaluating {
      new IntervalMembership(10, 10)
    } should produce[AssertionError]
  }

  it should "survive an invalid input" in {
    (imFunc.score(subject,Traversable(IndexedSeq((bogusValue))))) should equal (0.0)
  }

}