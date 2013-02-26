/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.workbench.model

/**
 * A user.
 */
trait User {

  @volatile private var currentProject: Option[Project] = None

  /**
   * The current workspace of this user.
   */
  def workspace: Workspace

  def projectOpen = currentProject.isDefined

  /**
   * The current project of this user.
   */
  def project = currentProject.getOrElse(throw new NoSuchElementException("No active project"))

  /**
   * Reset current project of this user.
   */
  def resetCurrentProject() {currentProject = None}

  /**
   * Sets the current project of this user.
   */
  def project_=(project: Project) {
    currentProject = Some(project)
  }
}

object User {
  var userManager: () => User = () => throw new Exception("No user manager registered")

  /**
   * Retrieves the current user.
   */
  def apply() = userManager()

  sealed trait Message

}