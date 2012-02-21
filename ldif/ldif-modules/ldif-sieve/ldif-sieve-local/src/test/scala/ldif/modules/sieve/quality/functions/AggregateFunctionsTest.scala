package ldif.modules.sieve.quality.functions

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Days}

/**
 * 
 * @author pablomendes
 */
@RunWith(classOf[JUnitRunner])
class AggregateFunctionsTest extends FlatSpec with ShouldMatchers {


  val tcFunc = new TimeCloseness(6)

  val node = new Node("", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
  val nodes = Traversable(IndexedSeq(node))

  it should "survive an invalid date" in {
    (tcFunc.score(nodesBad)) should equal (0.0)
  }


}