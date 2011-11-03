package ldif.local.runtime.impl

import java.io.{FileInputStream, BufferedInputStream, ObjectInputStream, File}
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 26.07.11
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */

class FileObjectReader[T >: Null](val inputFile: File, val endObject: T) {
  var objectInput: ObjectInputStream = null
  var closed = false
  var bufferedObject: T = null
  var buffered = false

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

  def size = throw new RuntimeException("Method 'size' not implemented in FileObjectReader")

  def close() = if(objectInput!=null) objectInput.close()

  private def openStream() {
    objectInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFile)))
  }
}