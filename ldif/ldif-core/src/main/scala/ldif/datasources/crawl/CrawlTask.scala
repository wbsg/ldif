package ldif.datasources.crawl

import ldif.module.ModuleTask
import ldif.util.Identifier
import java.net.URL

class CrawlTask(override val name : Identifier, val seed : URL) extends ModuleTask