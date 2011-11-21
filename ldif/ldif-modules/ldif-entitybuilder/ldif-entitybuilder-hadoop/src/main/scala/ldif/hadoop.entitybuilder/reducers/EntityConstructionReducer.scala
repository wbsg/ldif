package ldif.hadoop.entitybuilder.reducers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import java.util.Iterator
import collection.mutable.{HashMap, HashSet, ArrayBuffer}
import ldif.hadoop.types._
import ldif.hadoop.utils.HadoopHelper
import ldif.hadoop.entitybuilder.ResultBuilder
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

    val passesRestriction = resultBuilder.checkRestriction(entityDescriptionID, valuePaths)
    if(passesRestriction) {
      val result = resultBuilder.computeResultTables(entityDescriptionID, valuePaths)
      if(hasResults(entityDescriptionID, result)) {
        val expandedResult = expandResultsForRestrictionOnlyPatterns(entityDescriptionID, result)
        reporter.incrCounter("LDIF nr. of entities per ED", "ED ID "+key.entityDescriptionID.get(), 1)
        output.collect(key.entityDescriptionID, new EntityWritable(key.node, EntityWritable.convertResultTable(expandedResult), key.entityDescriptionID))
        //For Debugging
        val debugCollector = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[IntWritable, EntityWritable]]
        debugCollector.collect(key.entityDescriptionID, new EntityWritable(key.node, EntityWritable.convertResultTable(expandedResult), key.entityDescriptionID))
      }
    }
  }

  private def expandResultsForRestrictionOnlyPatterns(entityDescriptionID: Int, results: IndexedSeq[Traversable[IndexedSeq[NodeWritable]]]): IndexedSeq[Traversable[IndexedSeq[NodeWritable]]] = {
    val entityDescription = edmd.entityDescriptions(entityDescriptionID)
    val expandedResults = new ArrayBuffer[Traversable[IndexedSeq[NodeWritable]]]()
    for(patternIndex <- 0 until entityDescription.patterns.length) {
      if(entityDescription.patterns(patternIndex).length>0)
        expandedResults.append(results(patternIndex))
      else
        expandedResults.append(List(IndexedSeq[NodeWritable]()))
    }
    expandedResults
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