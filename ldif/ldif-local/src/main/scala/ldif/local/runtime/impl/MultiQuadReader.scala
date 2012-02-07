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

import ldif.runtime.Quad
import ldif.local.runtime.{QuadReader, ClonableQuadReader}
import collection.mutable.ArrayBuffer

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 21.06.11
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */

class MultiQuadReader(quadReaders: QuadReader*) extends ClonableQuadReader {
  var index = 0
  var closed = false

  def size = 0

  def hasNext: Boolean = {
    if(closed)
      return false

    if(quadReaders(index).hasNext)
      return true
    else {
      while(index < quadReaders.length && (!quadReaders(index).hasNext))
        index += 1

      if(index >= quadReaders.length) {
        closed = true
        return false }
      else
        return true
    }
  }

  def read(): Quad = {
    quadReaders(index).read
  }

  /**
   * Checks if all registered QuadReaders are FileQuadReaders
   */
  def checkForFileQuadReaders(): Boolean = {
    var isFileQuadReader = true
    for(quadReader <- quadReaders)
      isFileQuadReader = isFileQuadReader && quadReader.isInstanceOf[FileQuadReader]
    isFileQuadReader
  }

  def cloneReader: MultiQuadReader = {
    val readers = new ArrayBuffer[QuadReader]
    for(quadReader <- quadReaders) {
      quadReader match {
        case qr: ClonableQuadReader => readers.append(qr.cloneReader)
        case qr if (!qr.hasNext) => // If reader is empty don't clone it
        case qr => throw new RuntimeException("No ClonableQuadReader implementation: " + qr.getClass)
      }
    }
    new MultiQuadReader(readers: _*)
  }
}