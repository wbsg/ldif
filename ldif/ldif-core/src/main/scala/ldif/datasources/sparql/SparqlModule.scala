package ldif.datasources.sparql

import ldif.module.Module

class SparqlModule extends Module
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
   * The configuration of this module
   */
  override def config =
  {
    //TODO dummy
    SparqlConfig(EndpointConfig("http://www4.wiwiss.fu-berlin.de/drugbank/sparql") :: Nil)
  }

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