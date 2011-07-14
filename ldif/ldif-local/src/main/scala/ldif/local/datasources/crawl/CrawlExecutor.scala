package ldif.local.datasources.crawl

import ldif.module.Executor
import ldif.local.runtime._
import ldif.datasources.crawl.CrawlTask

/**
 * Executor for the crawling data source.
 */
class CrawlExecutor() extends Executor
{
  type TaskType = CrawlTask
  type InputFormat = NoDataFormat
  type OutputFormat = GraphFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : CrawlTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : CrawlTask) : GraphFormat = GraphFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : CrawlTask, reader : Null, writer : QuadWriter)
  {
    new CrawlLoader(task.seed).crawl(writer,task.levels,task.limit)
  }
}