package ldif.module

trait Module
{
  /**
   * The type the configuration of this module.
   */
  type ConfigType <: ModuleConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType <: ModuleTask

  /**
   * The configuration of this module
   */
  def config : ConfigType

  /**
   * Retrieves the tasks in this module.
   */
  def tasks : Traversable[TaskType]
}
