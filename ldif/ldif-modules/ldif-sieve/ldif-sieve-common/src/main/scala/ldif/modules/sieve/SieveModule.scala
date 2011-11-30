package ldif.modules.sieve

import ldif.module.Module
import java.io.File
import org.slf4j.LoggerFactory

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
  private val log = LoggerFactory.getLogger(getClass.getName)

  def load(file : File) : SieveModule =
  {
    //DefaultImplementations.register()

    val config = if(file==null || !file.exists()) SieveConfig.empty else loadConfig(file)
    new SieveModule(new SieveModuleConfig(config))
  }

  private def loadConfig(file : File) : SieveConfig =
  {
    if (file==null) log.debug("Trying to load null config file into Sieve. Returning empty config.");

    if(file!=null && file.isFile)
    {
      SieveConfig.load(file)
    }
    else if(file!=null && file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      SieveConfig.empty
    }
  }
}