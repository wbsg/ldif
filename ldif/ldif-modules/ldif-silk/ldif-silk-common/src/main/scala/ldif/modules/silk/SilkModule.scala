package ldif.modules.silk

import ldif.module.Module
import java.io.File
import de.fuberlin.wiwiss.silk.config.Configuration
import de.fuberlin.wiwiss.silk.impl.DefaultImplementations

/**
 * Silk Module.
 */
class SilkModule(val config : SilkModuleConfig) extends Module
{
  type ConfigType = SilkModuleConfig

  type TaskType = SilkTask

  lazy val tasks : Traversable[SilkTask] =
  {
    for(linkSpec <- config.silkConfig.linkSpecs) yield new SilkTask(config.silkConfig, linkSpec)
  }
}

object SilkModule
{
  def load(file : File) : SilkModule =
  {
    DefaultImplementations.register()

    new SilkModule(new SilkModuleConfig(loadConfig(file)))
  }

  private def loadConfig(file : File) : Configuration =
  {
    if(file.isFile)
    {
      Configuration.load(file)
    }
    else if(file.isDirectory)
    {
      file.listFiles.map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      Configuration.empty
    }
  }
}