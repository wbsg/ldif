/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

import xml.Node
import ldif.local.datasources.crawl.CrawlLoader
import java.net.URI
import java.io.OutputStream
import ldif.util.{JobMonitor, ReportPublisher, ImportJobStatusMonitor, Identifier}

case class CrawlImportJob(conf : CrawlConfig, id :  Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  val reporter = new CrawlImportJobPublisher(id)
  val crawler = new CrawlLoader(conf.seedUris, conf.predicatesToFollow, conf.renameGraphs, reporter)

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    JobMonitor.addPublisher(reporter)
    reporter.setStartTime()
    reporter.estimatedQuads = estimatedNumberOfQuads
    val limit = conf.resourceLimit
    val result = crawler.crawl(out, conf.levels, limit)
    importedQuadsNumber = result._1.toLong
    importedGraphs = result._2
    reporter.setFinishTime()
    true
  }

  override def getType = "crawl"
  override def getOriginalLocation = ""

  def toXML = {
    val xml = {
      <crawlImportJob>
        <seedURIs>
          {for (uri <- conf.seedUris) yield { <uri>{uri}</uri> } }
        </seedURIs>
        {if (conf.isAnyPredicateDefined){
            <predicatesToFollow>
              {for (uri <- conf.predicatesToFollow) yield { <uri>{uri}</uri> } }
            </predicatesToFollow>   }
        }
        {if(conf.isLevelsDefined) <levels>{conf.levels}</levels>}
        {if(conf.isResourceLimitDefined) <resourceLimit>{conf.resourceLimit}</resourceLimit>}
        {if(conf.isRenameGraphEnabled) <renameGraphs>{conf.renameGraphs}</renameGraphs>}
      </crawlImportJob>
    }
    toXML(xml)
  }
}

object CrawlImportJob {

  def fromXML(node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val levels = ((node \ "levels") text)
    val resourceLimitString =  ((node \ "resourceLimit") text)
    var resourceLimit: Int = -1
    if(resourceLimitString.length() > 0)
      resourceLimit = resourceLimitString.toInt
    val seedUris = (node \ "seedURIs" \ "uri").map(x => new URI(x text)).toTraversable
    val predicatesToFollow = (node \ "predicatesToFollow" \ "uri").map(x => new URI(x text)).toTraversable
    val renameGraphs = (node \ "renameGraphs" text)

    val crawlConfig = CrawlConfig(seedUris, predicatesToFollow, levels.toInt, resourceLimit, renameGraphs)

    val job = new CrawlImportJob(crawlConfig, id, refreshSchedule, dataSource)
    job
  }

}

case class CrawlConfig(seedUris : Traversable[URI], predicatesToFollow : Traversable[URI], levels : Int, resourceLimit: Int, renameGraphs : String){
  def isRenameGraphEnabled = renameGraphs != ""
  def isAnyPredicateDefined = predicatesToFollow.size > 0
  def isResourceLimitDefined = resourceLimit > 0
  def isLevelsDefined = levels > 0
}

class CrawlImportJobPublisher (id : Identifier) extends ImportJobStatusMonitor(id) with ReportPublisher {
  override def getPublisherName = super.getPublisherName + " (crawl)"
}

