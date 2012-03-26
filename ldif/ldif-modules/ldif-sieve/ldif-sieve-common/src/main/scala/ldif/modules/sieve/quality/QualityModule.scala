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
import ldif.modules.sieve.SieveConfig
import collection.mutable.HashMap

/**
 * Sieve Quality Assessment Module.
 */
class QualityModule(val config : QualityModuleConfig) extends Module with QualityAssessmentProvider
{

  private val log = LoggerFactory.getLogger(getClass.getName)

  type ConfigType = QualityModuleConfig

  type TaskType = QualityTask

  lazy val tasks : Traversable[QualityTask] = //automatically generates one task per spec
  {
    for(qualitySpec <- config.qualityConfig.qualitySpecs) yield {
      val task = new QualityTask(config, qualitySpec)
      qaMap.put(qualitySpec.outputPropertyNames.head,task.qualityAssessment)
      task
    }
  }

  // stores the assessment for underlying tasks
  val qaMap : HashMap[String,QualityAssessmentProvider] = new HashMap[String,QualityAssessmentProvider]()
  def putScore(propertyName: String, graph: String, score: Double) = {
    qaMap.get(propertyName) match {
      case Some(qa) => qa.putScore(propertyName, graph, score)
      case _ => log.error("Cannot put score. Requested quality metric %s has not been assessed by any of the available quality tasks.".format(propertyName))
    }
  }

  def size = {
    qaMap.values.foldLeft(0)((acc, qa) => acc + qa.size)
  }

  def getScore(propertyName: String, graph: String) = {
    qaMap.get(propertyName) match {
      case Some(qa) => qa.getScore(propertyName, graph)
      case _ => {
        log.error("Cannot get score. Requested quality metric %s has not been assessed by any of the available quality tasks. (%s)".format(propertyName,graph))
        0.0
      }
    }
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
      SieveConfig.load(file).qualityConfig
    }
    else if(file!=null && file.isDirectory && file.listFiles.size > 0)
    {
      file.listFiles.filter(p => !p.isDirectory).map(loadConfig).reduceLeft(_ merge _)
    }
    else
    {
      QualityConfig.empty
    }
  }
}