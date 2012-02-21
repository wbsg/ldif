package ldif.local.runtime.impl

import ldif.local.runtime.QuadReader
import ldif.runtime.QuadWriter

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 2/21/12
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This QuadReader writes all quads it returns also to a QuadWriter. The QuadWriter is NOT closed in the end.
 */
class CopyQuadReader(reader: QuadReader, writeTo: QuadWriter) extends QuadReader {
  def size = reader.size

  def read() = {
    val value = reader.read()
    writeTo.write(value)
    value
  }

  def hasNext = reader.hasNext
}