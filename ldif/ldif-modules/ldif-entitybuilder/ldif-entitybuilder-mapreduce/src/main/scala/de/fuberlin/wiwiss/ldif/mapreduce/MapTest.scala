package de.fuberlin.wiwiss.ldif.mapreduce

import org.apache.hadoop.mapreduce._
import org.apache.hadoop.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 21.07.11
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */


class MapTest extends Mapper[LongWritable, Text, Text, IntWritable] {
  def map(key: LongWritable, value: Text, context: Context) {
    val line = value.toString
    val year = line.substring(3,7)
    val airTemperature = Integer.parseInt(line.substring(10, 15))
    context.write(new Text(year), new IntWritable(airTemperature))
  }
}

