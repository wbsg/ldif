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
    log.info("Loaded "+  callback.graphs + "resources and "+ callback.statements + " statements")
    callback.graphs
  }

  // Crawl and write to a QuadQueue
  def crawl(quadWriter : QuadWriter, levels : Int, limit : Int) {
    val callback = new CallbackQuadQueue(quadWriter)
    crawl(callback, levels, limit)
    log.info("Loaded "+  callback.statements + " statements")
  }

  private def crawl(callback : Callback, levels : Int = 1, limit : Int = -1) {
    val sink = new SinkCallback(callback)

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
        log.info("Predicates to follow ("+predicates.size+"): " + predicates.mkString(" "))
        crawler.setLinkFilter(new LinkFilterSelect(frontier, predicateNodes, true))
      }

      crawler.setOutputCallback(sink)

      // Run the crawler (with Frontier frontier, int depth, int maxuris, int maxplds)
      crawler.evaluateBreadthFirst(frontier, levels, limit, -1, Mode.ABOX_AND_TBOX)

      log.info("Crawled seed: "+seed)
    }
  }
}
