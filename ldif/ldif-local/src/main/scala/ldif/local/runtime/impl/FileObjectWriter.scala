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