/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

import ldif.runtime.Quad
import ldif.local.runtime.ClonableQuadReader
import java.io._
import java.lang.RuntimeException
import ldif.entity.Entity

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 16.06.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

class FileQuadReader(inputFile: File) extends FileObjectReader[Quad](inputFile, NoQuadsLeft) with ClonableQuadReader {
  def cloneReader = new FileQuadReader(inputFile)
}