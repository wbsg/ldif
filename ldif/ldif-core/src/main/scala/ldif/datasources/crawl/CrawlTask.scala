package ldif.datasources.crawl

import ldif.module.ModuleTask
import ldif.util.Identifier
import java.net.URI

 /**
 * Crawl data access task
 *
 * @param levels Max number of levels to crawl (how deep the search is). Default value is 0
 * @param limit Max number of URI to crawl for each round/level for each PLD. Default value is -1
 */

class CrawlTask(override val name : Identifier, val seed : URI, val levels : Int = 0, val limit : Int  = -1) extends ModuleTask