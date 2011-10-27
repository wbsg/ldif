package de.fuberlin.wiwiss.ldif.mapreduce.reducers

import de.fuberlin.wiwiss.ldif.mapreduce.EntityDescriptionMetadata
import org.apache.hadoop.mapred.lib.MultipleOutputs
import de.fuberlin.wiwiss.ldif.mapreduce.utils.HadoopHelper
import org.apache.hadoop.mapred._
import collection.mutable.ArrayBuffer
import org.apache.hadoop.io.{Writable, IntWritable}
import de.fuberlin.wiwiss.ldif.mapreduce.types._
import java.util.Iterator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityConstructionReducer extends MapReduceBase with Reducer[EntityDescriptionNodeWritable, ValuePathWritable, IntWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
  }

  override def reduce(key: EntityDescriptionNodeWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    while(values.hasNext) {
      val value = values.next()
      output.collect(key.entityDescriptionID, value)
    }
  }
}