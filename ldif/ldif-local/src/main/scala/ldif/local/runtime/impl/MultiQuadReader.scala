package ldif.local.runtime.impl

import ldif.local.runtime.{Quad, QuadReader}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 21.06.11
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */

class MultiQuadReader(quadReader: QuadReader*) extends QuadReader {
  var index = 0
  var closed = false

  def size = 0

  def hasNext: Boolean = {
    if(closed)
      false

    if(quadReader(index).hasNext)
      return true
    else {
      while(index < quadReader.length && !quadReader(index).hasNext)
        index += 1

      if(index >= quadReader.length) {
        closed = true
        return false }
      else
        return true
    }
  }

  def read(): Quad = {
    quadReader(index).read
  }

  def isEmpty = false
}