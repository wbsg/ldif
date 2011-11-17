package ldif.hadoop.mappers

import org.apache.hadoop.mapred._
import ldif.datasources.dump.QuadParser
import lib.MultipleOutputs
import org.apache.hadoop.io._
import ldif.hadoop.types._
import ldif.entity.{EntityDescriptionMetadata, NodeWritable}
import ldif.hadoop.utils.HadoopHelper

class ProcessQuadsMapper extends MapReduceBase with Mapper[LongWritable, Text, IntWritable, ValuePathWritable] {
  private val parser = new QuadParser
  private val values = new NodeArrayWritable
  private var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null
  private var collector: OutputCollector[IntWritable, ValuePathWritable] = null
  private val phase: IntWritable = new IntWritable(0)

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def map(key: LongWritable, value: Text, output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    val quad = parser.parseLine(value.toString)
    if(quad==null)
      return
    val property = quad.predicate
    val propertyInfosValue = edmd.propertyMap.get(property)
    propertyInfosValue match {
      case None =>
      case Some(propertyInfos) =>
        for(propertyInfo <- propertyInfos) {
          val pathType = {
            if(edmd.pathLength(propertyInfo.pathId)==1)
              FinishedPathType
            else if (propertyInfo.phase==0) EntityPathType
            else JoinPathType
          }
          val subj = new NodeWritable(quad.subject)
          val obj = new NodeWritable((quad.value))
          if(propertyInfo.isForward)
            values.set(Array[Writable](subj, obj))
          else
            values.set(Array[Writable](obj, subj))

          if(pathType==FinishedPathType)
            phase.set(0)
          else
            phase.set(propertyInfo.phase)
          val path = new ValuePathWritable(new IntWritable(propertyInfo.pathId), pathType, values)
          collector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
          collector.collect(phase, path)
          collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
          collector.collect(phase, path)
        }
    }
  }

  override def close() {
    mos.close()
  }
}
