package ldif.local.datasources.crawl

import com.ontologycentral.ldspider.Crawler
import com.ontologycentral.ldspider.Crawler.Mode
import com.ontologycentral.ldspider.frontier.BasicFrontier
import java.net.{URISyntaxException, URI}
import java.util.logging.Logger
import com.ontologycentral.ldspider.hooks.sink.SinkCallback
import java.util.Calendar
import java.io.InputStream
import ldif.local.runtime.impl.{BlockingQuadQueue, QuadQueue}
import ldif.local.runtime.QuadWriter
import org.semanticweb.yars.util.CallbackNxOutputStream

/**
 * Streams data into temporary graph, crawling from given resource/URI seeds.
 **/

@throws(classOf[Exception])
class CrawlLoader(seed : URI) {
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

    //    if (!predicates.isEmpty()) {
    //      List<Node> predicateList = new ArrayList<Node>()
    //      val strPredicates : String = ""
    //      for (Node node : predicates) {
    //        strPredicates += node.toString()
    //        predicateList.add(node)
    //      }
    //      log.info("Predicates to crawl ("+predicates.size()+"): "+strPredicates)
    //      crawler.setLinkFilter(new LinkFilterSelect(frontier, predicateList, true))
    //    }

    crawler.setOutputCallback(sink)

    // Run the crawler (with Frontier frontier, int depth, int maxuris, int maxplds)
    crawler.evaluateBreadthFirst(frontier, levels, limit, -1, Mode.ABOX_AND_TBOX)

    log.info("Crawled seed: "+seed+" \nLoaded "+  callback.statements + " statements")
  }
}
