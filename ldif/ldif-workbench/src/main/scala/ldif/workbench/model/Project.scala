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

import ldif.util.Identifier
import modules.dataSource.DataSourceModule
import modules.ImportModule
import modules.integration.IntegrationModule
import ldif.config.SchedulerConfig

trait Project
{
  /**
   * The name of this project
   */
  val name : Identifier

  /**
   * Retrieves the project configuration.
   */
  def config : SchedulerConfig

  /**
   * Updates the project configuration.
   */
  def config_=(config : SchedulerConfig)

  /**
   * The import module which encapsulates all import jobs.
   */
  def importModule : ImportModule

  /**
   * The integration module.
   */
  def integrationModule : IntegrationModule

  /**
   * The dataSource module which encapsulates all data sources.
   */
  def dataSourceModule : DataSourceModule

}
