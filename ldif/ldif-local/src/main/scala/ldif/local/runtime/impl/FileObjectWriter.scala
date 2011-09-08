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
  val objectOutput = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))

  def finish = {write(endObject); objectOutput.flush(); objectOutput.close()}

  def write(obj: T) = {
    objectOutput.writeObject(obj)
  }
}