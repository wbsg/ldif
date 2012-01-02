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

package ldif.modules.r2r.hadoop

import ldif.module.ModuleTask
import ldif.util.Identifier
import de.fuberlin.wiwiss.r2r.LDIFMapping

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:20 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RHadoopTask(val ldifMappings: IndexedSeq[LDIFMapping]) extends ModuleTask{
  val name: Identifier = ldifMappings.head.mapping.getUri  //TODO use a proper identifier
}