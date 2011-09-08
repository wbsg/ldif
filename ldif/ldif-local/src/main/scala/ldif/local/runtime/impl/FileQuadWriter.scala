package ldif.local.runtime.impl

import ldif.local.runtime.QuadWriter
import java.io.File
import java.io.{ObjectOutputStream, FileOutputStream, BufferedOutputStream}
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 16.06.11
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */

class FileQuadWriter(val outputFile: File) extends QuadWriter {
  val objectOutput = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))

  def finish = {write(NoQuadsLeft); objectOutput.flush(); objectOutput.close()}

  def write(quad: Quad) = {
    objectOutput.writeObject(quad)
  }
}