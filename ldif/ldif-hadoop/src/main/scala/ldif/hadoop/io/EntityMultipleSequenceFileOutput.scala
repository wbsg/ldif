package ldif.hadoop.io

import org.apache.hadoop.io.IntWritable
import ldif.entity.EntityWritable
import org.apache.hadoop.mapred.lib.{MultipleSequenceFileOutputFormat, MultipleTextOutputFormat}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 6:08 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, EntityWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: EntityWritable, filename: String): String = {
    EntityMultipleSequenceFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object EntityMultipleSequenceFileOutput {
  def generateDirectoryName(entityDescriptionID: Int) = "eb_entities_for_ed_" + entityDescriptionID
}