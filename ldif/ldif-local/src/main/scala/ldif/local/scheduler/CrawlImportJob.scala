package ldif.local.scheduler

import ldif.util.Identifier
import xml.Node
import ldif.local.datasources.crawl.CrawlLoader
import java.net.URI
import java.io.OutputStream

case class CrawlImportJob(conf : CrawlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{

  val crawler = new CrawlLoader(conf.seedUris, conf.predicatesToFollow)

  override def load(out : OutputStream) {
    val limit = -1
    importedGraphs = crawler.crawl(out, conf.levels, limit)
  }

  override def getType = "crawl"
  override def getOriginalLocation = ""
}

object CrawlImportJob {

    def fromXML(node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
      val levels = (node \ "levels") text
      val seedUris = (node \ "seedURIs" \ "uri").map(x => new URI(x text)).toTraversable
      val predicatesToFollow = (node \ "predicatesToFollow" \ "uri").map(x => new URI(x text)).toTraversable

      val crawlConfig = CrawlConfig(seedUris, predicatesToFollow, levels.toInt)
      val job = new CrawlImportJob(crawlConfig, id, refreshSchedule, dataSource)
      job
    }

}

case class CrawlConfig(seedUris : Traversable[URI], predicatesToFollow : Traversable[URI], levels : Int) {

}