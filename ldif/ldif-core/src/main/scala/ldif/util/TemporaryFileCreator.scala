/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.util

import java.io.{IOException, File}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/8/11
 * Time: 7:22 PM
 * To change this template use File | Settings | File Templates.
 */

object TemporaryFileCreator {
  var tempDir = {
    val homeDir = System.getProperty("user.home")
    val ldifDir = new File(homeDir, ".ldiftmp")
    if (!ldifDir.exists())
      ldifDir.mkdirs()
    ldifDir
  }

  def createTemporaryFile(prefix: String, suffix: String, deleteOnExit: Boolean = true): File = {
    val tempFile = File.createTempFile(prefix, suffix, tempDir)
    if (deleteOnExit)
      tempFile.deleteOnExit()
    return tempFile
  }

  def createTemporaryDirectory(prefix: String, suffix: String, deleteOnExit: Boolean = true): File = {
    val tempFile = File.createTempFile(prefix, suffix, tempDir)
    tempFile.delete()
    tempFile.mkdir()
    if(deleteOnExit)
      tempFile.deleteOnExit()

    return tempFile
  }

  def deleteDirOnExit(dir: File) {
    dir.deleteOnExit()
    val files = dir.listFiles
    if (files != null) {
      for (file <- files)
        if (file.isDirectory())
          deleteDirOnExit(file)
        else
          file.deleteOnExit()
    }
  }

  def setNewTempDir(directory: File) {
    if (directory.isDirectory)
      tempDir = directory
    else
      throw new IOException("File " + directory.getAbsolutePath + " is no directory.")
  }
}