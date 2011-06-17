package ldif.local.runtime.impl

import ldif.local.runtime.{Quad, QuadReader}
import java.io.File
import java.io.{ObjectInputStream, FileInputStream, BufferedInputStream}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 16.06.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

class FileQuadReader extends QuadReader { //TODO: IMPLEMENT!
  def hasNext = false

  def read() = null

  def isEmpty = false

  def size = 0
}