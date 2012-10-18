package ldif.local.scheduler

import java.io.OutputStream
import ldif.util._
import net.liftweb.json._
import dispatch._
import org.apache.commons.httpclient.URI
import java.net.{URI, URLEncoder, URL}
import ldif.local.util.WikidataConsts
import ldif.entity.Node
import ldif.runtime.{Quad, Triple}
import java.io._
import org.apache.commons.httpclient.URI

/**
 * Created with IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */

class WikidataImportJob(val wikidataConfig: WikidataImportConfig, val id: Identifier, val refreshSchedule: String, val dataSource: String) extends ImportJob {
  val reporter = new WikidataImportJobPublisher(id)

  /**
   * Start import and write results to output stream. Return true on success and true and fail.
   */
  def load(out: OutputStream, estimatedNumberOfQuads: Option[Double]) = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    val writer = new ReportingOutputStreamWriter(out, reporter)
    val wikidata = new WikidataAccess(wikidataConfig.endpoint, wikidataConfig.limit)
    for(item <- wikidata)
      item match {
        case (id, item) if item.title.isDefined => {
          val itemUri = WikidataConsts.wikidataItemPrefix + item.title.get
          // Output labels
          for ((langTag, label) <- item.labels.getOrElse(Seq())) {
            if(wikidataConfig.filterLangs.isEmpty || wikidataConfig.filterLangs.get.contains(label.language)) {
              writer.write(Triple(Node.createUriNode(itemUri), Consts.RDFS_LABEL, Node.createLanguageLiteral(label.value, label.language)))
              reporter.importedQuads.incrementAndGet()
            }
          }
        }
        case error => println("ERROR: " + error)
      }
    writer.flush
    writer.close
    reporter.setFinishTime()
    true
  }

  def getType = "wikidata"

  def getOriginalLocation = "Wikidata API"

  def toXML = <wikidataImportJob>
    <endpoint>{wikidataConfig.endpoint}</endpoint>
    {if (wikidataConfig.limit.isDefined) <limit>{wikidataConfig.limit.get}</limit>}
    {if (wikidataConfig.filterLangs.isDefined) <langs>{wikidataConfig.filterLangs.get.mkString(" ")}</langs>}
  </wikidataImportJob>

}

object WikidataImportJob {

  def fromXML(node : xml.Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val endpoint = (node \ "endpoint" text).trim match {
      case "" => WikidataConsts.wikidataEndpoint
      case ep => ep
    }
    val limit = (node \ "limit" text).trim match {
      case "" => None
      case l => Some(l.toLong)
    }
    val langFilter = (node \ "langs" text).trim match {
      case "" => None
      case langs => Some(langs.split("""\s+""").toSet)
    }

    val wikidataImportConfig = WikidataImportConfig(endpoint, limit, langFilter)

    val job = new WikidataImportJob(wikidataImportConfig, id, refreshSchedule, dataSource)
    job
  }
}

case class WikidataImportConfig(endpoint: String = WikidataConsts.wikidataEndpoint, limit: Option[Long] = Some(100), filterLangs: Option[Set[String]] = None )

case class ItemListResult(query: ItemIDList, `query-continue`: Option[ContinueItemID])

case class ContinueItemID(allpages: ContinueID)

case class ContinueID(apcontinue: String)

case class ItemIDList(allpages: List[ItemID])

case class ItemID(pageid: Long, ns: Int, title: String)

case class ItemList(entities: Map[String, WikidataItem])

case class WikidataItem(id: Option[String], `type`: Option[String], pageid: Option[Long], title: Option[String], labels: Option[Map[String, Label]])

case class Label(language: String, value: String)

class WikidataAccess(endpoint: String, itemLimit: Option[Long] = None) {
  implicit val formats = DefaultFormats
  private var count = 0
  private var currentItemList: Option[ItemListResult] = None
  private var currentItemListIndex = 0
  private val maxListItems = 500
  private var currentItems: Option[List[(String, WikidataItem)]] = None
  private var currentItemIndex = 0
  private val maxReturnItems = 50

  def foreach(f: Pair[String, WikidataItem] => Unit) {
    var current: Option[(String, WikidataItem)] = next
    while(current!=None && (itemLimit==None) || count < itemLimit.get){
      while(current!=None && current.get._2.title==None)
        current = next
      current.map(f(_))
      count += 1
      current = next
    }
  }

  private def requestItemList(startWithTitle: Option[String] = None): ItemListResult = {
    val query = "action=query&list=allpages&aplimit=" + maxListItems + "&format=json" + (if(startWithTitle!=None) "&apfrom=" + startWithTitle.get else "")
    val json = getJSON(query)
    json.extract[ItemListResult]
  }

  private def requestNextItemList(itemList: ItemListResult): Option[ItemListResult] = {
    itemList.`query-continue` match {
      case None => None
      case Some(ContinueItemID(ContinueID(continueTitle))) => Some(requestItemList(Some(continueTitle)))
    }
  }

  private def getNextItemIds(): Option[Seq[Long]] = {
    currentItemList match {
      case None => currentItemList = Some(requestItemList())
        currentItemListIndex = 0
      case Some(ItemListResult(ItemIDList(items), continueItem)) => if (items.size <= currentItemListIndex) {
        currentItemList = requestNextItemList(currentItemList.get)
        currentItemListIndex = 0
      }
    }
    if (currentItemList==None)
      return None
    val itemIds = currentItemList.get.query.allpages.slice(currentItemListIndex, currentItemListIndex+maxReturnItems).map(_.pageid)
    currentItemListIndex += maxReturnItems
    Some(itemIds)
  }

  private def getNextItems(): Option[List[(String, WikidataItem)]] = {
    getNextItemIds() match {
      case None => return None
      case Some(itemIds) =>
        val query = "action=wbgetentities&format=json&ids=" + itemIds.mkString("%7C")
        val json = getJSON(query)
        Some(json.extract[ItemList].entities.toList)
    }
  }

  def next: Option[(String, WikidataItem)] = {
    currentItems match {
      case None => currentItems = getNextItems()
        currentItemIndex = 0
      case Some(items) => if (items.size <= currentItemIndex) {
        currentItems = getNextItems()
        currentItemIndex = 0
      }
    }
    if(currentItems==None)
      return None
    currentItemIndex += 1
    Some(currentItems.get.apply(currentItemIndex-1))
  }

  private def getJSON(query: String): JValue = {
    val requestURL = url(endpoint + "?" + query)
    JsonParser.parse(Http(requestURL OK as.String)())
  }
}

object WikidataAccess {
  def main(args: Array[String]) {
    val wikidata = new WikidataAccess("http://wikidata-test-repo.wikimedia.de/w/api.php")//, Some(30))
    printToFile(new File("wikidata.nq"))(p => {
      for(item <- wikidata)
        item match {
          case (id, item) if item.title.isDefined => {
            val itemUri = WikidataConsts.wikidataItemPrefix + item.title.get
            for ((langTag, label) <- item.labels.getOrElse(Seq()))
              p.println(Triple(Node.createUriNode(itemUri), Consts.RDFS_LABEL, Node.createLanguageLiteral(label.value, label.language)).toNQuadFormat + " .")
          }
          case error => println("ERROR: " + error)
        }
      })

      Http.shutdown()
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}

class WikidataImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {

  //var limit : Int = 0
  var actualLimit : Int = 0

  override def getPublisherName = super.getPublisherName + " (Wikidata)"


}