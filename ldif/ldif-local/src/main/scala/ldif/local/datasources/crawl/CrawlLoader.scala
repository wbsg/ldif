package ldif.local.datasources.crawl

import com.ontologycentral.ldspider.Crawler
import com.ontologycentral.ldspider.Crawler.Mode
import com.ontologycentral.ldspider.frontier.BasicFrontier
import java.net.{URISyntaxException, URI}
import java.util.logging.Logger
import com.ontologycentral.ldspider.hooks.sink.SinkCallback
import ldif.local.runtime.QuadWriter
import com.ontologycentral.ldspider.hooks.links.LinkFilterSelect
import java.util.ArrayList
import org.semanticweb.yars.nx.{Resource, Node}

/**
 * Streams data into temporary graph, crawling from given resource/URI seeds.
 **/

@throws(classOf[Exception])
class CrawlLoader(seed : URI, predicates : Traversable[URI] = Traversable.empty[URI]) {
  private val log = Logger.getLogger(getClass.getName)

  def crawl(quadWriter : QuadWriter, levels : Int = 1, limit : Int = -1) = {

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

    val callback = new CallbackQuadQueue(quadWriter)
    val sink = new SinkCallback(callback)

    // Add predicate filter
    if (!predicates.isEmpty) {
      val predicateNodes = new ArrayList[Node]
      for (predicate <- predicates) {
        predicateNodes.add(new Resource(predicate.toString))
      }
      log.info("Predicates to crawl ("+predicates.size+"): " + predicates.mkString(" "))
      crawler.setLinkFilter(new LinkFilterSelect(frontier, predicateNodes, true))
    }

    crawler.setOutputCallback(sink)

    // Run the crawler (with Frontier frontier, int depth, int maxuris, int maxplds)
    crawler.evaluateBreadthFirst(frontier, levels, limit, -1, Mode.ABOX_AND_TBOX)

    log.info("Crawled seed: "+seed+" \nLoaded "+  callback.statements + " statements")
  }
}
