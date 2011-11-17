package ldif.hadoop.mappers

import org.apache.hadoop.io.{IntWritable}
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import ldif.hadoop.types._
import ldif.entity.{EntityDescriptionMetadata, NodeWritable}
import ldif.hadoop.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathJoinMapper extends MapReduceBase with Mapper[IntWritable, ValuePathWritable, PathJoinValueWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null
  private var collector: OutputCollector[IntWritable, ValuePathWritable] = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  def map(key: IntWritable, value: ValuePathWritable, output: OutputCollector[PathJoinValueWritable, ValuePathWritable], reporter: Reporter) {
    if(value.pathType==FinishedPathType) {
      collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
      collector.collect(key, value)
      collector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
      collector.collect(key, value)
    }
    else {
      val nodes = value.values.get
      if(value.pathType==EntityPathType)
        output.collect(new PathJoinValueWritable(value.pathID, nodes(nodes.length-1).asInstanceOf[NodeWritable]), value)
      else
        output.collect(new PathJoinValueWritable(value.pathID, nodes(0).asInstanceOf[NodeWritable]), value)
    }
  }

  override def close() {
    mos.close()
  }
}