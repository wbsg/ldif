package ldif.workbench.model.file

/*
 * LDIF
 *
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

import java.io.File
import org.slf4j.LoggerFactory
import ldif.workbench.model.util.FileUtils._
import ldif.workbench.model.{User, Workspace, Project}
import ldif.workbench.model.modules.ImportTask
import ldif.util.Identifier
import ldif.local.scheduler.{DataSource, ImportJob}
import ldif.workbench.model.modules.dataSource.DataSourceTask

class FileWorkspace(file: File) extends Workspace {
  private val log = LoggerFactory.getLogger(getClass.getName)

  file.mkdir()

  private var projectList: List[Project] = {
    for (projectDir <- file.listFiles.filter(_.isDirectory).toList) yield {
      log.info("Loading project: " + projectDir)
      new FileProject(projectDir)
    }
  }

  override def projects: List[Project] = projectList

  override def createProject(name: Identifier) = {
    require(!projectList.exists(_.name == name), "A project with the name '" + name + "' already exists")
    val projectDir = file + ("/" + name)
    projectDir.mkdir()
    val newProject = new FileProject(projectDir)
    projectList ::= newProject
    newProject
  }

  override def removeProject(name: Identifier) = {
    (file + ("/" + name)).deleteRecursive()
    projectList = projectList.filterNot(_.name == name)
  }

  override def saveDataSource(name: Identifier, xml: String) {
    val job = DataSource.fromString(xml)
    User().project.dataSourceModule.update(DataSourceTask(job))
  }

  override def saveImportJob(name: Identifier, xml: String) {
    val job = ImportJob.fromString(xml)
    User().project.importModule.update(ImportTask(job))
  }

  override def importImportJob(file: File) {
    println(file.getCanonicalPath)
    log.info(file.getCanonicalPath)
    val job = ImportJob.fromFile(file)
    User().project.importModule.update(ImportTask(job))
  }
}