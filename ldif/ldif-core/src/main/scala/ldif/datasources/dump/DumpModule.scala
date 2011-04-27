package ldif.datasources.dump

import ldif.module.Module

class DumpModule (override val config : DumpConfig)  extends Module
{
  /**
   * The type the configuration of this module.
   */
  type ConfigType = DumpConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType = DumpTask

  /**
   * Retrieves the tasks in this module.
   */
  override val tasks : Traversable[DumpTask] =
  {
    for((sourceLocation, index) <- config.sourceLocations.toSeq.zipWithIndex) yield
    {
      new DumpTask("Dump" + index, sourceLocation)
    }
  }
}