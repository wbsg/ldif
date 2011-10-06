package de.fuberlin.wiwiss.ldif.mapreduce

import org.apache.hadoop.mapreduce.Mapper
import ldif.datasources.dump.QuadParser
import ldif.entity.NodeWritable
import org.apache.hadoop.io._

class ProcessQuadsMapper(edmd: EntityDescriptionMetadata) extends Mapper[LongWritable, Text, IntWritable, ValuePathWritable] {
  val parser = new QuadParser
  val values = new ArrayWritable(classOf[NodeWritable])

  def map(key: LongWritable, value: Text, context: Context) {
    val quad = parser.parseLine(value.toString)
    val property = quad.predicate
    val propertyInfos = edmd.propertyMap(property)
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
