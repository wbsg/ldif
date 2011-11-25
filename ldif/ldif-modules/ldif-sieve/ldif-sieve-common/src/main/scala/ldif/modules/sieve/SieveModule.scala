package ldif.modules.sieve

import ldif.module.Module
import java.io.File

/**
 * Sieve Module.
 */
class SieveModule(val config : SieveModuleConfig) extends Module
{
  type ConfigType = SieveModuleConfig

  type TaskType = SieveTask

  lazy val tasks : Traversable[SieveTask] =
  {
    for(sieveSpec <- config.sieveConfig.sieveSpecs) yield new SieveTask(config, sieveSpec)
  }
}

object SieveModule
{
  def load(file : File) : SieveModule =
  {
    //DefaultImplementations.register()

    new SieveModule(new SieveModuleConfig(loadConfig(file)))
  }

  private def loadConfig(file : File) : SieveConfig =
  {
    if(file.isFile)
    {
      SieveConfig.load(file)
    }
    else if(file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      SieveConfig.empty
    }
  }
}