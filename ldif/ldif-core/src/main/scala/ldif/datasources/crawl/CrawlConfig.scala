package ldif.datasources.crawl

import ldif.module.ModuleConfig
import java.net.URI

case class CrawlConfig (seeds : Traversable[URI]) extends ModuleConfig


