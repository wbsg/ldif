package de.fuberlin.wiwiss.ldif.mapreduce.mappers

import de.fuberlin.wiwiss.ldif.mapreduce._
import org.apache.hadoop.mapreduce.Mapper
import ldif.datasources.dump.QuadParser
import ldif.entity.NodeWritable
import org.apache.hadoop.io._
import de.fuberlin.wiwiss.ldif.mapreduce.types._

class ProcessQuadsMapper extends Mapper[LongWritable, Text, IntWritable, ValuePathWritable] {
  val parser = new QuadParser
  val values = new NodeArrayWritable

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, IntWritable, ValuePathWritable]#Context) {
    val quad = parser.parseLine(value.toString)
    if(quad==null)
      return
    val property = quad.predicate
    val propertyInfos = List(PropertyInfo(0,0,true));//edmd.propertyMap(property)
    if(propertyInfos!=null) {
      for(propertyInfo <- propertyInfos) {
        val pathType = if (propertyInfo.phase==0) EntityPathType else JoinPathType
        val subj = new NodeWritable(quad.subject)
        val obj = new NodeWritable((quad.value))
        if(propertyInfo.isForward)
          values.set(Array(subj, obj))
        else
          values.set(Array(obj, subj))
        context.write(new IntWritable(propertyInfo.phase),
          new ValuePathWritable(new IntWritable(propertyInfo.pathId), pathType, values))
      }

       //TODO: Implement MultiOutput
    }
  }
}
