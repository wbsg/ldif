package ldif.util

import ldif.runtime.QuadReader
import java.io.{FileWriter, BufferedWriter, File}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 5/24/12
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */

object QuadUtils {
  def dumpQuadReaderToFile(reader: QuadReader, file: File) {
    val writer = new BufferedWriter(new FileWriter(file))
    for(quad <- reader)
      writer.append(quad.toLine)
    writer.flush()
    writer.close()
  }

  def dumpQuadReaderToFile(reader: QuadReader, file: String) {
    dumpQuadReaderToFile(reader, new File(file))
  }
}