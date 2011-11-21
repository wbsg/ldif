/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.r2r.hadoop

import ldif.module.Module
import ldif.modules.r2r.R2RConfig

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopModule(val config: R2RConfig) extends Module {
  type ConfigType = R2RConfig

  type TaskType = R2RHadoopTask

  def tasks: Traversable[R2RHadoopTask] = {
    List(new R2RHadoopTask(config.ldifMappings))
  }
}