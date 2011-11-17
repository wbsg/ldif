/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.modules.silk

import ldif.module.Module
import java.io.File
import de.fuberlin.wiwiss.silk.impl.DefaultImplementations
import de.fuberlin.wiwiss.silk.config.SilkConfig

/**
 * Silk Module.
 */
class SilkModule(val config : SilkModuleConfig) extends Module
{
  type ConfigType = SilkModuleConfig

  type TaskType = SilkTask

  lazy val tasks : Traversable[SilkTask] =
  {
    for(linkSpec <- config.silkConfig.linkSpecs) yield new SilkTask(config, linkSpec)
  }
}

object SilkModule
{
  def load(file : File) : SilkModule =
  {
    DefaultImplementations.register()

    new SilkModule(new SilkModuleConfig(loadConfig(file)))
  }

  private def loadConfig(file : File) : SilkConfig =
  {
    if(file.isFile)
    {
      SilkConfig.load(file)
    }
    else if(file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      SilkConfig.empty
    }
  }
}