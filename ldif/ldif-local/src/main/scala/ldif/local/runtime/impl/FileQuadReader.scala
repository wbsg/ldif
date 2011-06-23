package ldif.local.runtime.impl

import ldif.local.runtime.{Quad, QuadReader}
import java.io._
import java.lang.RuntimeException

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 16.06.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

class FileQuadReader(val inputFile: File) extends QuadReader {
  val objectInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFile)))
  var closed = false
  var bufferedQuad: Quad = null
  var buffered = false

  def hasNext: Boolean = {
    if(closed)
      return false

    if(buffered)
      return true

    val o = objectInput.readObject()
    if(!o.isInstanceOf[Quad])
      throw new RuntimeException("FileQuadReader read non-Quad object from stream. Object of class " + o.getClass + ": " + o)

    val quad = o.asInstanceOf[Quad]
    if(quad==NoQuadsLeft) {
      closed = true
      objectInput.close()
      false
    } else {
      bufferedQuad = quad
      buffered = true
      true
    }
  }

  def read(): Quad = {
    if(buffered) {
      buffered = false
      return bufferedQuad
    }

    if(hasNext)
      read()
    else
      throw new RuntimeException("No Quads left in FileQuadReader! Use hasNext-method before calling read-method.")
  }

  def isEmpty = !hasNext

  def size = throw new RuntimeException("Method 'size' not implemented in FileQuadReader")

  def close() = objectInput.close()
}