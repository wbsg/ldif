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

package ldif.datasources.sparql

import ldif.module.Module

class SparqlModule (override val config : SparqlConfig)  extends Module
{
  /**
   * The type the configuration of this module.
   */
  type ConfigType = SparqlConfig

  /**
   * The type of the tasks of this module
   */
  type TaskType = SparqlTask

  /**
   * Retrieves the tasks in this module.
   */
  override val tasks : Traversable[SparqlTask] =
  {
    for((endpointUrl, index) <- config.endpointUrls.toSeq.zipWithIndex) yield
    {
      new SparqlTask("Sparql" + index, endpointUrl, config.entityDescriptions)
    }
  }
}