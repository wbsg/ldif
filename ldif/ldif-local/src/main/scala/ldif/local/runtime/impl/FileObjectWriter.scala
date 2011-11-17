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
import java.io.{File, FileOutputStream, BufferedOutputStream, ObjectOutputStream}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 14:58
 * To change this template use File | Settings | File Templates.
 */

class FileObjectWriter[T <: AnyRef](val outputFile: File, val endObject: T) {
  var counter = 0
  var objectOutput: ObjectOutputStream = null

  def finish = { write(endObject); objectOutput.reset(); objectOutput.flush(); objectOutput.close()}

  def write(obj: T) = {
    // to reduce number of open files
    if(objectOutput==null) openStream()

    objectOutput.writeObject(obj)
    counter += 1
    if(counter % 1000 == 0)
      objectOutput.reset()
  }

  private def openStream() {
    objectOutput = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))
  }
}