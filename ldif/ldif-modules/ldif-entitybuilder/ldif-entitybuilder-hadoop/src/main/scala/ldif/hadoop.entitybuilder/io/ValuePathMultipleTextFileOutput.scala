package ldif.hadoop.entitybuilder.io

import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types.{FinishedPathType, ValuePathWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/26/11
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathMultipleTextFileOutput extends MultipleTextOutputFormat[IntWritable, ValuePathWritable] {
  val fileSeparator = System.getProperty("file.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    var fileName = ""
    if(value.pathType==FinishedPathType)
      fileName = ValuePathMultipleTextFileOutput.generateDirectoryNameForFinishedValuePaths(key.get)
    else
      fileName = ValuePathMultipleTextFileOutput.generateDirectoryNameForValuePathsInConstruction(key.get)
    fileName + fileSeparator + filename
  }
}

object ValuePathMultipleTextFileOutput {
  def generateDirectoryNameForValuePathsInConstruction(phase: Int) = "text_eb_construct_valuepath_iteration_" + phase
  def generateDirectoryNameForFinishedValuePaths(phase: Int) = "text_eb_finished_valuepath_iteration_" + phase
}