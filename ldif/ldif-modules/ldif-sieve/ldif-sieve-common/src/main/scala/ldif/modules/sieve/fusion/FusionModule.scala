package ldif.modules.sieve.fusion

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
import ldif.modules.sieve.SieveConfig
import ldif.modules.sieve.quality.QualityAssessmentProvider

/**
 * Sieve Module.
 */
class FusionModule(val config : FusionModuleConfig, val qualityProvider: QualityAssessmentProvider) extends Module
{

  type ConfigType = FusionModuleConfig

  type TaskType = FusionTask

  lazy val tasks : Traversable[FusionTask] = //automatically generates one task per spec
  {
    for(fusionSpec <- config.fusionConfig.fusionSpecs) yield new FusionTask(config, fusionSpec, qualityProvider)
  }
}

object FusionModule
{
  private val log = LoggerFactory.getLogger(getClass.getName)

  def load(file : File, quality: QualityAssessmentProvider) : FusionModule =
  {
    //DefaultImplementations.register()

    val config = if(file==null || !file.exists()) FusionConfig.empty else loadConfig(file)
    new FusionModule(new FusionModuleConfig(config), quality)
  }

  private def loadConfig(file : File) : FusionConfig =
  {
    if (file==null) log.debug("Trying to load null config file into Sieve. Returning empty config.");

    if(file!=null && file.isFile)
    {
      SieveConfig.load(file).fusionConfig
    }
    else if(file!=null && file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.filter(p => !p.isDirectory).map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      FusionConfig.empty
    }
  }
}