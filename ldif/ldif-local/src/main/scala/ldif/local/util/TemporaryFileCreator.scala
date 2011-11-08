package ldif.local.util

import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/8/11
 * Time: 7:22 PM
 * To change this template use File | Settings | File Templates.
 */

object TemporaryFileCreator {
  def createTemporaryFile(prefix: String, suffix: String, deleteOnExit: Boolean): File = {
    val tempFile = File.createTempFile(prefix, suffix)
    if(deleteOnExit)
      tempFile.deleteOnExit()
    return tempFile
  }

  def createTemporaryDirectory(prefix: String, suffix: String, deleteOnExit: Boolean): File = {
    val tempFile = File.createTempFile(prefix, suffix)
    if(deleteOnExit)
      tempFile.deleteOnExit()
    tempFile.delete()
    tempFile.mkdir()

    return tempFile
  }
}