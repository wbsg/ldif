package de.fuberlin.wiwiss.ldif.mapreduce.reducers

import de.fuberlin.wiwiss.ldif.mapreduce.EntityDescriptionMetadata
import org.apache.hadoop.mapred.lib.MultipleOutputs
import de.fuberlin.wiwiss.ldif.mapreduce.utils.HadoopHelper
import org.apache.hadoop.mapred._
import org.apache.hadoop.io.{Writable, IntWritable}
import de.fuberlin.wiwiss.ldif.mapreduce.types._
import java.util.Iterator
import collection.mutable.{HashMap, HashSet, ArrayBuffer}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityConstructionReducer extends MapReduceBase with Reducer[EntityDescriptionNodeWritable, ValuePathWritable, IntWritable, EntityWritable] {
  var edmd: EntityDescriptionMetadata = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
  }

  override def reduce(key: EntityDescriptionNodeWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, EntityWritable], reporter: Reporter) {
    val entityDescriptionID = key.entityDescriptionID.get
    val patternPaths = new HashMap[Int, ValuePathWritable]
    val restrictionPathValues = new HashSet[ValuePathWritable]
    while(values.hasNext) {
      val value = values.next()
      output.collect(key.entityDescriptionID, new EntityWritable())
    }
  }
}