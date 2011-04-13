package ldif.datasources.sparql

import ldif.module.Module

class SparqlModule(override val config : SparqlConfig) extends Module
{
  /**
   * The type the configuration of this module.
   */
  type ConfigType = SparqlConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType = SparqlTask

  /**
   * Retrieves the tasks in this module.
   */
  override val tasks : Traversable[SparqlTask] =
  {
    for((endpoint, index) <- config.endpoints.toSeq.zipWithIndex) yield
    {
      new SparqlTask("Sparql" + index, endpoint)
    }
  }
}