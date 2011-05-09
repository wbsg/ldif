package ldif.modules.silk

import ldif.module.Module

/**
 * Silk Module.
 */
class SilkModule(val config : SilkConfig) extends Module
{
  type ConfigType = SilkConfig

  type TaskType = SilkTask

  lazy val tasks : Traversable[SilkTask] =
  {
    for(linkSpec <- config.silkConfig.linkSpecs) yield new SilkTask(config.silkConfig, linkSpec)
  }
}