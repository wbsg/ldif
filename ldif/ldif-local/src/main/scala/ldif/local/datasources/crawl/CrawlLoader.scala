/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local.datasources.crawl

import com.ontologycentral.ldspider.Crawler
import com.ontologycentral.ldspider.Crawler.Mode
import com.ontologycentral.ldspider.frontier.BasicFrontier
import java.net.{URISyntaxException, URI}
import java.util.logging.Logger
import ldif.local.runtime.QuadWriter
import com.ontologycentral.ldspider.hooks.links.LinkFilterSelect
import org.semanticweb.yars.nx.{Resource, Node}
import com.ontologycentral.ldspider.hooks.sink.SinkCallback
import java.io.OutputStream
import ldif.local.scheduler.CallbackOutputStream
import java.util.ArrayList
import org.semanticweb.yars.nx.parser.Callback
import collection.mutable.Set

/**
 * Streams data into temporary graph, crawling from given resource/URI seeds.
 **/

@throws(classOf[Exception])
class CrawlLoader(seedUris : Traversable[URI], predicates : Traversable[URI] = Traversable.empty[URI]) {
  private val log = Logger.getLogger(getClass.getName)

  /** Crawl and write to a file
   *
   * @return set of imported pages/graphs
   */
  def crawl(out : OutputStream, levels : Int, limit : Int) : Set[String] = {
    val callback = new CallbackOutputStream(out)
    crawl(callback, levels, limit)
    log.info("Loaded "+  callback.graphs.size + " resources and "+ callback.statements + " statements")
    callback.graphs
  }

  // Crawl and write to a QuadQueue
  def crawl(quadWriter : QuadWriter, levels : Int, limit : Int) {
    val callback = new CallbackQuadQueue(quadWriter)
    crawl(callback, levels, limit)
    log.info("Loaded "+  callback.statements + " statements")
  }

  private def crawl(callback : Callback, levels : Int = 1, limit : Int = -1, includeProvenance :Boolean = false) {
    val sink = new SinkCallback(callback, includeProvenance)

    for (seed <- seedUris){
      log.info("Crawling seed: "+seed+ " (with levels="+levels+", limit="+limit+")")

      // Initialize the crawler (with number of threads)
      val crawler = new Crawler(10)

      // Initialize the frontier with the seed
      val frontier = new BasicFrontier()
      try {
        frontier.add(seed)
      } catch {
        case e:URISyntaxException => {
          log.warning("URISyntaxException: " + e.getMessage)
        }
      }

      // Add predicate filter
      if (!predicates.isEmpty) {
        val predicateNodes = new ArrayList[Node]
        for (predicate <- predicates) {
          predicateNodes.add(new Resource(predicate.toString))
        }
        log.fine("Predicates to follow ("+predicates.size+"): " + predicates.mkString(" "))
        crawler.setLinkFilter(new LinkFilterSelect(frontier, predicateNodes, true))
      }

      crawler.setOutputCallback(sink)

      // Run the crawler (with Frontier frontier, int depth, int maxuris, int maxplds)
      crawler.evaluateBreadthFirst(frontier, levels, limit, -1, Mode.ABOX_AND_TBOX)

      log.fine("Crawled seed: "+seed)
    }
  }
}
