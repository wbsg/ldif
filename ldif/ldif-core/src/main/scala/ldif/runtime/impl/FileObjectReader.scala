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

package ldif.runtime.impl

import java.io.{FileInputStream, BufferedInputStream, ObjectInputStream, File}
import ldif.runtime.Quad
import java.util.zip.GZIPInputStream

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */

class FileObjectReader[T >: Null](val inputFile: File, val endObject: T, val compression: Boolean = false, numberOfObjects: Int = 0) {
  var objectInput: ObjectInputStream = null
  var closed = false
  var bufferedObject: T = null
  var buffered = false
  private var nrOfObjects = numberOfObjects

  def hasNext: Boolean = {
    if(closed)
      return false

    if(buffered)
      return true

    // Keep number of open files small
    if(objectInput==null) openStream()
    val o = objectInput.readObject()

    if(!o.isInstanceOf[T])
      throw new RuntimeException("FileObjectReader read invalid object from stream. Object of class " + o.asInstanceOf[AnyRef].getClass + ": " + o)

    val obj = o.asInstanceOf[T]
    if(obj==endObject) {
      closed = true
      close()
      false
    } else {
      bufferedObject = obj
      buffered = true
      true
    }
  }

  def read(): T = {
    if(nrOfObjects>0)
      nrOfObjects -= 1
    if(buffered) {
      buffered = false
      return bufferedObject
    }

    if(hasNext)
      read()
    else
      throw new RuntimeException("No objects left in FileObjectReader! Use hasNext-method before calling read-method.")
  }

  def isEmpty = !hasNext

  def size = nrOfObjects

  def close() = if(objectInput!=null) objectInput.close()

  private def openStream() {
    if(compression)
      objectInput = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(inputFile)), 8*1024))
    else
      objectInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFile)))
  }
}