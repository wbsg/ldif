package ldif.local.datasources.crawl

import ldif.datasources.dump.DumpTask
import ldif.module.Executor
import ldif.local.runtime._

/**
 * Executor for the dump data source.
 */
class CrawlExecutor() extends Executor
{
  type TaskType = DumpTask
  type InputFormat = NoDataFormat
  type OutputFormat = GraphFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : DumpTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : DumpTask) : GraphFormat = GraphFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : DumpTask, reader : Null, writer : QuadWriter)
  {
  }
}