package ldif.modules.silk

import ldif.module.Module
import java.io.File
import de.fuberlin.wiwiss.silk.config.Configuration
import de.fuberlin.wiwiss.silk.impl.DefaultImplementations

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

object SilkModule
{
  def load(file : File) : SilkModule =
  {
    DefaultImplementations.register()

    if(file.isFile)
    {
      new SilkModule(new SilkConfig(Configuration.load(file)))
    }
    else
    {
      throw new UnsupportedOperationException("Loading link specifications from a directory not supported yet")
    }
  }
}