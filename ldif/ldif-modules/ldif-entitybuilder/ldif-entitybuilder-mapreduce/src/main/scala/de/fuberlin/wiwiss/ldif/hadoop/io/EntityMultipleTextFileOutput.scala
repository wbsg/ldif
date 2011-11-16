package de.fuberlin.wiwiss.ldif.mapreduce.io

import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.io.{NullWritable, IntWritable}
import ldif.entity.EntityWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityMultipleTextFileOutput extends MultipleTextOutputFormat[IntWritable, EntityWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: EntityWritable, filename: String): String = {
    EntityMultipleTextFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object EntityMultipleTextFileOutput {
  def generateDirectoryName(entityDescriptionID: Int) = "text_eb_entity_for_ed_" + entityDescriptionID
}