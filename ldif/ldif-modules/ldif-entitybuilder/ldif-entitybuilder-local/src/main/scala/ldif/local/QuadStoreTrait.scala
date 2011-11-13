package ldif.local

import java.io.{IOException, File}
import ldif.entity.EntityDescription
import ldif.local.runtime.EntityWriter
import org.apache.commons.io.FileUtils

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 15:03
 * To change this template use File | Settings | File Templates.
 */

trait QuadStoreTrait {

  /**
   * clear the database
   */
  def clearDatabase

  /**
   * Load a dataset into the quad store
   */
  def loadDataset(datasetFile: File)

  /**
   * Write all the entities conforming to the entity description to the entity writer
   */
  def queryStore(entityDescription: EntityDescription, entityWriter: EntityWriter): Boolean

  def createTemporaryDatabaseDirectory(rootDirectory: String): File = {
    val rootDir = new File(rootDirectory)
    FileUtils.forceMkdir(rootDir)
    var tempDir: File = null

    tempDir = File.createTempFile("database_", System.nanoTime().toString, rootDir);

    if(!(tempDir.delete())) {
      throw new IOException("Could not delete temp file: " + tempDir.getAbsolutePath());
    }

    if(!(tempDir.mkdir())) {
      throw new IOException("Could not create temp directory: " + tempDir.getAbsolutePath());
    }
    tempDir.deleteOnExit

    return tempDir;
  }
}