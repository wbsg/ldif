package ldif.local.scheduler

import ldif.util.Identifier
import xml.Node
import java.io.Writer


case class CrawlImportJob(conf : CrawlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{

  override def load(writer : Writer) {
    //TODO
  }

  override def getType = "crawl"
  override def getOriginalLocation = ""
}

object CrawlImportJob {

  //  def fromXML(node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
  //    val crawlConfig : CrawlConfig =
  //    val job = new CrawlImportJob(crawlConfig, id, refreshSchedule, dataSource)
  //    job
  //  }

}

class CrawlConfig(seedUris : Seq[String], predicatesToCrawl : Seq[String], levels : Int) {

}