package de.fuberlin.wiwiss.ldif.mapreduce.reducers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import java.util.Iterator
import collection.mutable.{HashMap, HashSet, ArrayBuffer}
import ldif.mapreduce.types._
import ldif.mapreduce.utils.HadoopHelper
import de.fuberlin.wiwiss.ldif.mapreduce.ResultBuilder
import org.apache.hadoop.io.{WritableUtils, Writable, IntWritable}
import org.apache.hadoop.conf.Configuration
import ldif.entity.{NodeWritable, EntityDescriptionMetadata, EntityWritable}

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
  private var mos: MultipleOutputs = null
  private var config: Configuration = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    resultBuilder = new ResultBuilder(edmd)
    // For debugging
    mos = new MultipleOutputs(conf)
    config = conf
  }

  override def reduce(key: EntityDescriptionNodeWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, EntityWritable], reporter: Reporter) {
    val entityDescriptionID = key.entityDescriptionID.get

    val valuePaths = new ArrayBuffer[ValuePathWritable]()
    while(values.hasNext) {
      val value = values.next()
      valuePaths.append(WritableUtils.clone(value, config))
    }
    // For debugging
//    for(value <- valuePaths) {
//      val collector = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//      collector.collect(key.entityDescriptionID, value)
//    }
    val passesRestriction = resultBuilder.checkRestriction(entityDescriptionID, valuePaths)
    if(passesRestriction) {
      val result = resultBuilder.computeResultTables(entityDescriptionID, valuePaths)
      if(hasResults(entityDescriptionID, result))
        output.collect(key.entityDescriptionID, new EntityWritable(key.node, EntityWritable.convertResultTable(result), key.entityDescriptionID))
    }
  }

  private def hasResults(entityDescriptionID: Int, results: IndexedSeq[Traversable[IndexedSeq[NodeWritable]]]): Boolean = {
    val entityDescription = edmd.entityDescriptions(entityDescriptionID)
    for(patternIndex <- 0 until entityDescription.patterns.length)
      if(entityDescription.patterns(patternIndex).length>0 && results(patternIndex).size==0)
        return false
    return true
  }

  override def close() {
    mos.close()
  }
}