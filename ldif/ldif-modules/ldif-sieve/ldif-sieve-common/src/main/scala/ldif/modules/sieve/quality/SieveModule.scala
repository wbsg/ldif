package ldif.modules.sieve.quality

/*
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import ldif.module.Module
import java.io.File
import org.slf4j.LoggerFactory

/**
 * Sieve Quality Assessment Module.
 */
class QualityModule(val config : QualityModuleConfig) extends Module
{

  type ConfigType = QualityModuleConfig

  type TaskType = QualityTask

  lazy val tasks : Traversable[QualityTask] = //automatically generates one task per spec
  {
    for(sieveSpec <- config.qualityConfig.qualitySpecs) yield new QualityTask(config, sieveSpec)
  }
}

object QualityModule
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  def load(file : File) : QualityModule =
  {
    //DefaultImplementations.register()

    val config = if(file==null || !file.exists()) QualityConfig.empty else loadConfig(file)
    new QualityModule(new QualityModuleConfig(config))
  }

  private def loadConfig(file : File) : QualityConfig =
  {
    if (file==null) log.debug("Trying to load null config file into Sieve. Returning empty config.");

    if(file!=null && file.isFile)
    {
      QualityConfig.load(file)
    }
    else if(file!=null && file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      QualityConfig.empty
    }
  }
}