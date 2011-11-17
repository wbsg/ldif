package ldif.hadoop.io

import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.{FinishedPathType, ValuePathWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, ValuePathWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    var fileName = ""
    if(value.pathType==FinishedPathType)
      fileName = ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(key.get)
    else
      fileName = ValuePathMultipleSequenceFileOutput.generateDirectoryNameForValuePathsInConstruction(key.get)
    fileName + fileSeparator + filename
  }

}

object ValuePathMultipleSequenceFileOutput {
  def generateDirectoryNameForValuePathsInConstruction(phase: Int) = "eb_construct_valuepath_iteration_" + phase
  def generateDirectoryNameForFinishedValuePaths(phase: Int) = "eb_finished_valuepath_iteration_" + phase
}