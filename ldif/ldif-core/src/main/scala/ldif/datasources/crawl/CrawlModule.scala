package ldif.datasources.crawl

import ldif.module.Module

class CrawlModule (override val config : CrawlConfig)  extends Module
{
  /**
   * The type the configuration of this module.
   */
  type ConfigType = CrawlConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType = CrawlTask

  /**
   * Retrieves the tasks in this module.
   */
  override val tasks : Traversable[CrawlTask] =
  {
    for((seed, index) <- config.seeds.toSeq.zipWithIndex) yield
    {
      new CrawlTask("Crawl" + index, seed)
    }
  }
}