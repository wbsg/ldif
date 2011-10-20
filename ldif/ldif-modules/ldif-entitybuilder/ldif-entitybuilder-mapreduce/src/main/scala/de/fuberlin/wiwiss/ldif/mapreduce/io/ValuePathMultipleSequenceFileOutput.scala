package de.fuberlin.wiwiss.ldif.mapreduce.io

import org.apache.hadoop.mapred.lib.MultipleSequenceFileOutputFormat
import de.fuberlin.wiwiss.ldif.mapreduce.types.ValuePathWritable
import org.apache.hadoop.io.IntWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/18/11
 * Time: 1:18 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathMultipleSequenceFileOutput extends MultipleSequenceFileOutputFormat[IntWritable, ValuePathWritable] {
  val pathSeparator = System.getProperty("path.separator")
  override def generateFileNameForKeyValue(key: IntWritable, value: ValuePathWritable, filename: String): String = {
    "eb_join_iteration_" + key.get() + pathSeparator + filename
  }

}