package ldif

import ldif.module.Module

class EntityBuilderModule (override val config : EntityBuilderConfig) extends Module {

  /**
   * The type the configuration of this module.
   */
  type ConfigType = EntityBuilderConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType = EntityBuilderTask

  /**
   * Retrieves the tasks in this module.
   * Entity Builder has only one task.
   */
  override val tasks : Traversable[EntityBuilderTask] =
    Traversable(new EntityBuilderTask("EntityBuilderTask", config.entityDescriptions))
  
}