package de.fuberlin.wiwiss.ldif.mapreduce.utils

import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.filecache.DistributedCache
import org.apache.hadoop.fs.FileUtil
import java.io._
import java.net.URI
import org.apache.hadoop.conf.Configuration

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 4:59 PM
 * To change this template use File | Settings | File Templates.
 */

object HadoopHelper {
  val tempDir = new File(System.getProperty("java.io.tmpdir"))
  def distributeSerializableObject(objectToDistribute: Object, conf: Configuration) {
    val tempFile = File.createTempFile("ldif_hadoop_", "ser", tempDir)
    tempFile.deleteOnExit()
    val objectWriter = new ObjectOutputStream(new FileOutputStream(tempFile))
    objectWriter.writeObject(objectToDistribute)
    objectWriter.close()
    DistributedCache.addCacheFile(tempFile.toURI, conf)
  }

  def getDistributedFiles(conf: Configuration): Array[URI] = DistributedCache.getCacheFiles(conf)
}