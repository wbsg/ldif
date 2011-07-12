package ldif.datasources.crawl

import ldif.module.ModuleConfig
import java.net.URL

case class CrawlConfig (seeds : Traversable[URL]) extends ModuleConfig


