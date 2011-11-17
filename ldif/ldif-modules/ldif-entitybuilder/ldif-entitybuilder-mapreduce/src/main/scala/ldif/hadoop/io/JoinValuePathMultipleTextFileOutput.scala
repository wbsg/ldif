package ldif.hadoop.io

import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.ValuePathWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/26/11
 * Time: 1:42 PM
 * To change this template use File | Settings | File Templates.
 */

class JoinValuePathMultipleTextFileOutput extends MultipleTextOutputFormat[IntWritable, ValuePathWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    JoinValuePathMultipleTextFileOutput.generateDirectoryName(key.get) + fileSeparator + filename
  }
}

object JoinValuePathMultipleTextFileOutput {
  def generateDirectoryName(phase: Int) = "text_eb_join_iteration_" + phase
}