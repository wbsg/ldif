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

package ldif.workbench.model.file

import java.io.File
import org.slf4j.LoggerFactory
import ldif.workbench.model.util.FileUtils._
import ldif.workbench.model.util.XMLUtils._
import ldif.workbench.model.Project
import ldif.local.scheduler.ImportJob
import ldif.workbench.model.modules._
import dataSource.{DataSourceTask, DataSourceConfig, DataSourceModule}
import integration.{IntegrationTask, IntegrationModule, IntegrationConfig}
import ldif.local.IntegrationJob
import ldif.config.SchedulerConfig
import ldif.util.{ConfigProperties, Identifier}
import ldif.local.scheduler.DataSource

/**
 * Implementation of a project which is stored on the local file system.
 */
class FileProject(file : File) extends Project {
  private val log = LoggerFactory.getLogger(getClass.getName)

  private var cachedConfig : Option[SchedulerConfig] = None

  override val name : Identifier = file.getName

  /**
   * Reads the project configuration.
   */
  override def config = {
// if(cachedConfig.isEmpty) {
//      val configFile = file + "/config.xml"
//      if(configFile.exists) {                   //TODO do we really need a schedulerConfig file?
        val schedulerConfig = SchedulerConfig(
          importJobsFiles = (file + "/import").listFiles().toTraversable.filter(_.isFile),
          integrationJob = (file + "/integration/integrationJob.xml"),
          dataSourcesFiles = (file + "/datasources").listFiles().toTraversable.filter(_.isFile),
          dumpLocationDir = (file+"/dumps").getCanonicalPath,
          properties = ConfigProperties.loadProperties(file + "/scheduler.properties")
        )
        cachedConfig = Some(schedulerConfig)
//      }
//      else {
//        cachedConfig = Some(SchedulerConfig.empty)
//      }
//    }
    cachedConfig.get
  }

  /**
   * Writes the updated project configuration.
   */
  override def config_=(config : SchedulerConfig) {
   // config.toXML.write(file + "/config.xml")      //TODO
    cachedConfig = Some(config)
  }

  override val dataSourceModule : DataSourceModule = new FileDataSourceModule(file + "/datasources")

  override val importModule : ImportModule = new FileImportModule(file + "/import")

  override val integrationModule : IntegrationModule = new FileIntegrationModule(file + "/integration")


  /**
   * The import module which encapsulates all import jobs.
   */
  class FileImportModule(file: File) extends ImportModule {
    file.mkdirs()

    def config = ImportConfig()

    def config_=(c: ImportConfig) {}

    override def tasks = synchronized {
      for (configFile <- file.listFiles) yield {
        val job = ImportJob.load(configFile)
        ImportTask(job)
      }
    }

    override def update(task: ImportTask) = synchronized {
      task.job.toXML.write(file + ("/" + task.name + ".xml"))
      log.info("Updated import '" + task.name + "' in project '" + name + "'")
    }

    override def remove(taskId: Identifier) = synchronized {
      (file + ("/" + taskId + ".xml")).deleteRecursive()
      log.info("Removed import '" + taskId + "' from project '" + name + "'")
    }
  }

  /**
   * The integration module.
   */
  class FileIntegrationModule(file: File) extends IntegrationModule {
    file.mkdirs()

    def config = IntegrationConfig()

    def config_=(c: IntegrationConfig) {}

    override def tasks = synchronized {
      for (configFile <- file.listFiles.filter(_.isFile).filter(_.getName.endsWith(".xml")).toList) yield {
        var config = ldif.config.IntegrationConfig.load(configFile)
        // Set sources directory (forced)
        config = config.copy(sources = Seq((file.getParentFile+"/dumps").getCanonicalPath))
        val job = IntegrationJob(config)
        IntegrationTask(job)
      }
    }

    override def update(task: IntegrationTask) = synchronized {
      task.job.toXML.write(file + ("/" + task.name + ".xml"))
      log.info("Updated import '" + task.name + "' in project '" + name + "'")
    }

    override def remove(taskId: Identifier) = synchronized {
      (file + ("/" + taskId + ".xml")).deleteRecursive()
      log.info("Removed import '" + taskId + "' from project '" + name + "'")
    }
  }


  /**
   * The dataSource module  which encapsulates all data sources.
   */
  class FileDataSourceModule(file: File) extends DataSourceModule {
    file.mkdirs()

    def config = DataSourceConfig()

    def config_=(c: DataSourceConfig) {}

    override def tasks = synchronized {
      for (configFile <- file.listFiles) yield {
        DataSourceTask(DataSource.load(configFile))
      }
    }

    override def update(task: DataSourceTask) = synchronized {
      task.datasource.toXML.write(file + ("/" + task.name + ".xml"))
      log.info("Updated dataSource '" + task.name + "' in project '" + name + "'")
    }

    override def remove(taskId: Identifier) = synchronized {
      (file + ("/" + taskId + ".xml")).deleteRecursive()
      log.info("Removed dataSource '" + taskId + "' from project '" + name + "'")
    }
  }


}