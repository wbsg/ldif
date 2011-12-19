/* 
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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