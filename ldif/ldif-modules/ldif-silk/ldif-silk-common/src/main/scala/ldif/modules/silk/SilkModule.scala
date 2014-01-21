/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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
import de.fuberlin.wiwiss.silk.plugins.Plugins
import de.fuberlin.wiwiss.silk.config.LinkingConfig
import ldif.util.CommonUtils

/**
 * Silk Module.
 */
class SilkModule(val config : SilkModuleConfig) extends Module {

  type ConfigType = SilkModuleConfig

  type TaskType = SilkTask

  lazy val tasks : Traversable[SilkTask] = {
    for(linkSpec <- config.silkConfig.linkSpecs) yield new SilkTask(config, linkSpec)
  }
}

object SilkModule {

  def load(file : File) : SilkModule = {
    Plugins.register()

    new SilkModule(new SilkModuleConfig(loadConfig(file)))
  }

  private def loadConfig(file : File) : LinkingConfig = {
    if(file!=null && file.isFile)
      LinkingConfig.load(file)
    else if(file !=null && file.isDirectory && file.listFiles.size > 0)
      CommonUtils.listFiles(file,"xml").map(loadConfig).reduceLeft(_ merge _)
    else
      LinkingConfig.empty
  }
}