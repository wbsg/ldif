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