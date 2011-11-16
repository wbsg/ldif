package de.fuberlin.wiwiss.ldif.mapreduce.mappers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types._
import ldif.entity.{EntityDescriptionMetadata, NodeWritable}
import ldif.hadoop.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityConstructionMapper extends MapReduceBase with Mapper[IntWritable, ValuePathWritable, EntityDescriptionNodeWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  val entityDescriptionID = new IntWritable()
  val entityDescriptionNode = new EntityDescriptionNodeWritable()
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def map(key: IntWritable, value: ValuePathWritable, output: OutputCollector[EntityDescriptionNodeWritable, ValuePathWritable], reporter: Reporter) {
    entityDescriptionID.set(edmd.pathMap(value.pathID.get).entityDescriptionIndex)
    entityDescriptionNode.set(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable])
//    val collector = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//    collector.collect(entityDescriptionID, value)
    output.collect(new EntityDescriptionNodeWritable(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable]), value)
  }

  override def close() {
    mos.close()
  }
}