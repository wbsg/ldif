package ldif.hadoop.entitybuilder.io

import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.ValuePathWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/18/11
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */

class JoinValuePathMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, ValuePathWritable] {
  val fileSeparator = System.getProperty("file.separator")

  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object JoinValuePathMultipleSequenceFileOutput {
  def generateDirectoryName(phase: Int) = "eb_join_iteration_" + phase
}