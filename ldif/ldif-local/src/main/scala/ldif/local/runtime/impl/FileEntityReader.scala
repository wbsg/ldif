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

package ldif.local.runtime.impl

import java.io.File
import ldif.local.runtime.EntityReader
import ldif.runtime.impl.FileObjectReader
import ldif.entity.{EntityDescription, Entity}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:13
 * To change this template use File | Settings | File Templates.
 */

class FileEntityReader(val entityDescription : EntityDescription, inputFile: File, enableCompression: Boolean = false) extends FileObjectReader[Entity](inputFile, NoEntitiesLeft, enableCompression) with EntityReader {

  def this(entityWriter : FileEntityWriter) = {
    this(entityWriter.entityDescription, entityWriter.inputFile, entityWriter.enableCompression)
    this.factumBuilder = entityWriter.factumBuilder
  }

}
