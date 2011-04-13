package ldif.local.datasources.sparql

import ldif.datasources.sparql.SparqlTask
import ldif.local.runtime.{CacheWriter, DynamicInstanceFormat, NoDataFormat}
import ldif.module.Executor

/**
 * Executor for the SPARQL data source.
 */
class SparqlExecutor() extends Executor
{
  type TaskType = SparqlTask
  type InputFormat = NoDataFormat
  type OutputFormat = DynamicInstanceFormat

  /**
   * Determines the accepted input format of a specific task.
   */
  override def input(task : SparqlTask) : NoDataFormat = NoDataFormat()

  /**
   * Determines the output format of a specific task.
   */
  override def output(task : SparqlTask) : DynamicInstanceFormat = DynamicInstanceFormat()

  /**
   * Executes a specific task.
   *
   * @param task The rask to be executed
   * @param reader The reader of the input data
   * @param writer The writer of the output data
   */
  override def execute(task : SparqlTask, reader : Nothing, writer : CacheWriter)
  {
    val endpoint = new RemoteSparqlEndpoint(task.endpoint)

    val resourceRetriever = EntityRetriever(endpoint, task.endpoint.pageSize, task.endpoint.graph)

    val resources = resourceRetriever.retrieve(writer.entityDescription)

    for(resource <- resources)
    {
      writer.write(resource)
    }
  }
}
