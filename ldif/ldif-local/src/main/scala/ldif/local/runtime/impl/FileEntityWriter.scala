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

package ldif.local.runtime.impl

import java.io.File
import ldif.local.runtime.EntityWriter
import ldif.runtime.impl.FileObjectWriter
import ldif.entity.{FactumBuilder, EntityDescription, Entity}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */

class FileEntityWriter(val entityDescription : EntityDescription, val inputFile: File, enableCompression: Boolean = false) extends FileObjectWriter[Entity](inputFile, NoEntitiesLeft, enableCompression) with EntityWriter {
  var factumBuilder : FactumBuilder = null
  def setFactumBuilder(fb : FactumBuilder) { factumBuilder = fb  }
}