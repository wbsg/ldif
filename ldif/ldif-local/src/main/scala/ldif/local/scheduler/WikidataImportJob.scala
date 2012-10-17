package ldif.local.scheduler

import java.io.OutputStream
import ldif.util._
import net.liftweb.json._
import dispatch._

/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */

class WikidataImportJob(val id: Identifier, val refreshSchedule: String, val dataSource: String, endpoint: String = "http://wikidata-test-repo.wikimedia.de/w/api.php", limit: Option[Long]) extends ImportJob {
  val reporter = new WikidataImportJobPublisher(id)

  /**
   * Start import and write results to output stream. Return true on success and true and fail.
   */
  def load(out: OutputStream, estimatedNumberOfQuads: Option[Double]) = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    val writer = new ReportingOutputStreamWriter(out, reporter)

    writer.flush
    writer.close
    reporter.setFinishTime()
    true
  }

  def getType = "wikidata"

  def getOriginalLocation = "Wikidata API"

  def toXML = <wikidataImportJob>
    {if (limit!=None) <limit>{limit.get}</limit>}
  </wikidataImportJob>

}

case class ItemListResult(query: ItemList, querycontinue: Option[ContinueItem])

case class ContinueItem(allpages: ContinueID)

case class ContinueID(apcontinue: String)

case class ItemList(allpages: List[ItemID])

case class ItemID(pageid: Long, ns: Int, title: String)

case class WikidataItem(id: Long, pageid: Long, title: String, labels: Map[String, Label])

case class Label(language: String, value: String)

class WikidataAccess(endpoint: String, itemLimit: Option[Long] = None) {
  implicit val formats = DefaultFormats
  private var currentTitle: Option[String] = None
  private var count = 0

//  def foreach(f: WikidataItem => Unit) {
//    while ( { hasNext } ) {
//      f(read)
//    }
//  }

  def requestItemList(startWithTitle: Option[String] = None): ItemListResult = {
    val query = "action=query&list=allpages&aplimit=500&format=json" + (if(startWithTitle!=None) "&apfrom=" + startWithTitle.get else "")
    val json = getJSON(query)
    json.extract[ItemListResult]
  }

  def requestNextItemList(itemList: ItemListResult): Option[ItemListResult] = {
    itemList.querycontinue match {
      case None => None
      case Some(ContinueItem(ContinueID(continueTitle))) => Some(requestItemList(Some(continueTitle)))
    }
  }

  def getJSON(query: String): JValue = {
    val requestURL = url(endpoint + "?" + query)
    JsonParser.parse(Http(requestURL OK as.String)())
  }
}

object WikidataAccess {
  def main(args: Array[String]) {
    for( i <- new WikidataAccess("http://wikidata-test-repo.wikimedia.de/w/api.php").requestItemList().query.allpages)
      println(i)
    Http.shutdown()
  }
}

class WikidataImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {

  //var limit : Int = 0
  var actualLimit : Int = 0

  override def getPublisherName = super.getPublisherName + " (Wikidata)"


}