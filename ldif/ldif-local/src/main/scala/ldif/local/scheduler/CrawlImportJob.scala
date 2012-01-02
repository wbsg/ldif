/* 
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local.scheduler

import ldif.util.Identifier
import xml.Node
import ldif.local.datasources.crawl.CrawlLoader
import java.net.URI
import java.io.OutputStream

case class CrawlImportJob(conf : CrawlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob{

  val crawler = new CrawlLoader(conf.seedUris, conf.predicatesToFollow)

  override def load(out : OutputStream) : Boolean = {
    val limit = conf.resourceLimit
    importedGraphs = crawler.crawl(out, conf.levels, limit)
    true
  }

  override def getType = "crawl"
  override def getOriginalLocation = ""
}

object CrawlImportJob {

    def fromXML(node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
      val levels = (node \ "levels") text
      val resourceLimitString =  ((node \ "resourceLimit") text)
      var resourceLimit: Int = -1
      if(resourceLimitString.length() > 0)
        resourceLimit = resourceLimitString.toInt
      val seedUris = (node \ "seedURIs" \ "uri").map(x => new URI(x text)).toTraversable
      val predicatesToFollow = (node \ "predicatesToFollow" \ "uri").map(x => new URI(x text)).toTraversable

      val crawlConfig = CrawlConfig(seedUris, predicatesToFollow, levels.toInt, resourceLimit)
      val job = new CrawlImportJob(crawlConfig, id, refreshSchedule, dataSource)
      job
    }

}

case class CrawlConfig(seedUris : Traversable[URI], predicatesToFollow : Traversable[URI], levels : Int, resourceLimit: Int)