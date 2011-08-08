package ldif.local.datasources.sparql

import ldif.module.Executor
import ldif.local.runtime._
import ldif.datasources.sparql.SparqlTask

/**
 * Executor for the sparql data access module.
 */
class SparqlExecutor() extends Executor
{
  type TaskType = SparqlTask
  type InputFormat = NoDataFormat
  type OutputFormat = DynamicEntityFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : SparqlTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : SparqlTask) : DynamicEntityFormat = DynamicEntityFormat()

  /**
   * Executes a specific task.
   *
   * @param task The task to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : SparqlTask, reader : Null, writers : Seq[EntityWriter])
  {
    val seb = new SparqlEntityBuilder(task.endpointUrl)

    for ((ed, i) <- task.entityDescriptions.zipWithIndex )
      seb.buildEntities(ed, writers(i))
  }
}