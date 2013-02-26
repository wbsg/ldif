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

package ldif.hadoop.utils

import org.apache.hadoop.filecache.DistributedCache
import java.io._
import org.apache.hadoop.conf.Configuration
import ldif.entity.EntityDescriptionMetadata
import org.apache.hadoop.fs.{FileUtil, Path, FileSystem}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */

object HadoopHelper {
  val tempDir = new File(System.getProperty("java.io.tmpdir"))

  def distributeSerializableObject(objectToDistribute: Object, conf: Configuration, id: String) {
    val tempFile = File.createTempFile("ldif_hadoop_", id, tempDir)
    //tempFile.deleteOnExit()
    val objectWriter = new ObjectOutputStream(new FileOutputStream(tempFile))
    objectWriter.writeObject(objectToDistribute)
    objectWriter.close()

    // copy  local file to hdfs
    val fs = FileSystem.get(conf)
    val hdfsPath = new Path(tempFile.getCanonicalPath)
    if (!fs.exists(hdfsPath))
      FileUtil.copy(tempFile,fs, hdfsPath, false, conf)

    // add file to distributed cache
    DistributedCache.addCacheFile(hdfsPath.toUri, conf)
  }

  private def getDistributedFilePathForID(conf: Configuration, id: String): String = {
    val files = DistributedCache.getCacheFiles(conf)

    if(files!=null)
      for(file <- files) {
        if(file.toString.endsWith(id))
          return file.getPath
      }
    throw new RuntimeException("No distributed file with ID=" + id + " found!")
  }

  def getEntityDescriptionMetaData(conf: Configuration): EntityDescriptionMetadata = {
    try {
      val file = HadoopHelper.getDistributedFilePathForID(conf, "edmd")
      val fs = FileSystem.get(conf)
      val inputStream = fs.open(new Path(file))
      new ObjectInputStream(inputStream).readObject().asInstanceOf[EntityDescriptionMetadata]
    } catch {
      case e: RuntimeException => throw new RuntimeException("No Entity Description Meta Data found/distributed. Reason: " + e.getMessage)
    }
  }

  def getDistributedObject(conf: Configuration, id: String): AnyRef = {
    try {
      val file = HadoopHelper.getDistributedFilePathForID(conf, id)
      val fs = FileSystem.get(conf)
      val inputStream = fs.open(new Path(file))
      new ObjectInputStream(inputStream).readObject()
    } catch {
      case e: RuntimeException => throw new RuntimeException("No Entity Description Meta Data found/distributed. Reason: " + e.getMessage)
    }
  }
}