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
  override def config = new SparqlConfig() //dummy

  /**
   * Retrieves the tasks in this module.
   */
  override val tasks : Traversable[TaskType] =
  {
    null
  }
}