package de.fuberlin.wiwiss.ldif.mapreduce.mappers

import de.fuberlin.wiwiss.ldif.mapreduce.EntityDescriptionMetadata
import org.apache.hadoop.mapred.lib.MultipleOutputs
import de.fuberlin.wiwiss.ldif.mapreduce.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.entity.NodeWritable
import de.fuberlin.wiwiss.ldif.mapreduce.types.{EntityDescriptionNodeWritable, PathJoinValueWritable, ValuePathWritable}
import org.apache.hadoop.io.IntWritable

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

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
  }

  override def map(key: IntWritable, value: ValuePathWritable, output: OutputCollector[EntityDescriptionNodeWritable, ValuePathWritable], reporter: Reporter) {
    entityDescriptionID.set(edmd.pathMap(value.pathID.get).entityDescriptionIndex)
    entityDescriptionNode.set(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable])
    output.collect(new EntityDescriptionNodeWritable(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable]), value)
  }
}