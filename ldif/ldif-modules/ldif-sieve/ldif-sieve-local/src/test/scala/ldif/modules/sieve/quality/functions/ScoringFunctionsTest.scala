package ldif.modules.sieve.quality.functions

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Node
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import org.joda.time.{DateTime, DateTimeComparator, Days}

/**
 * 
 * @author pablomendes
 */
@RunWith(classOf[JUnitRunner])
class ScoringFunctionsTest extends FlatSpec with ShouldMatchers {

  val tcXml =   <ScoringFunction class="TimeCloseness">
                    <Param name="timeSpan" value="6"/>
                    <Input path="?GRAPH/provenance:lastUpdated"/>
                </ScoringFunction>


  val tcFunc = new TimeCloseness(6)

  val today = new DateTime()
  val todayMinus2 = new Node(today.minusDays(2).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
  val todayMinus3 = new Node(today.minusDays(3).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")
  val todayMinus4 = new Node(today.minusDays(4).toString(ISODateTimeFormat.dateTimeNoMillis()), "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")

  val nodes3 = Traversable(IndexedSeq(todayMinus3))
  val nodes4 = Traversable(IndexedSeq(todayMinus4,todayMinus3))
  val nodes2 = Traversable(IndexedSeq(todayMinus2,todayMinus3,todayMinus4))

  val nodesBad = Traversable(IndexedSeq(new Node("2012-sssssasas02-19T16:55:17Z", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graphId")))

  "ScoringFunctions" should "return the correct implementation given XML" in {
    (TimeCloseness.fromXML(tcXml)) should equal (tcFunc)
  }

  "ScoringFunctions" should "correctly score input values" in {
    (tcFunc.score(nodes3)) should equal (0.5)
  }

  "ScoringFunctions" should "correctly sort before scoring input values" in {
    (tcFunc.score(nodes4)) should equal (1.0 - (2.0/3))
    (tcFunc.score(nodes2)) should equal (1.0 - (2.0/3))
  }

  "ScoringFunctions" should "parse date" in {
    //val parser = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss Z")
    val parser =  ISODateTimeFormat.dateTimeNoMillis();
    val startDate = parser.parseDateTime("2012-02-19T16:55:17Z")
    val endDate = parser.parseDateTime("2012-02-20T16:55:17Z")
    val d = Days.daysBetween(startDate, endDate).getDays;
    (d) should equal (1.0)
  }

  "ScoringFunctions" should "survive an invalid date" in {
    (tcFunc.score(nodesBad)) should equal (0.0)
  }

}