package de.fuberlin.wiwiss.ldif.mapreduce.reducers

import org.apache.hadoop.mapred._
import org.apache.hadoop.io.{Writable, IntWritable}
import java.util.Iterator
import collection.mutable.{HashMap, HashSet, ArrayBuffer}
import ldif.mapreduce.types._
import ldif.entity.{EntityDescriptionMetadata, EntityWritable}
import ldif.mapreduce.utils.HadoopHelper
import de.fuberlin.wiwiss.ldif.mapreduce.ResultBuilder

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityConstructionReducer extends MapReduceBase with Reducer[EntityDescriptionNodeWritable, ValuePathWritable, IntWritable, EntityWritable] {
  var edmd: EntityDescriptionMetadata = null
  var resultBuilder: ResultBuilder = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    resultBuilder = new ResultBuilder(edmd)
  }

  override def reduce(key: EntityDescriptionNodeWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, EntityWritable], reporter: Reporter) {
    val entityDescriptionID = key.entityDescriptionID.get

    val valuePaths = new ArrayBuffer[ValuePathWritable]()
    while(values.hasNext)
      valuePaths.append(values.next())
    val result = resultBuilder.computeResultTables(entityDescriptionID, valuePaths)
    output.collect(key.entityDescriptionID, new EntityWritable(key.node, EntityWritable.convertResultTable(result), key.entityDescriptionID))
  }
}