/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.hadoop.entitybuilder.reducers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import java.util.Iterator
import ldif.hadoop.types._
import ldif.hadoop.utils.HadoopHelper
import ldif.hadoop.entitybuilder.ResultBuilder
import org.apache.hadoop.io.{WritableUtils, IntWritable}
import org.apache.hadoop.conf.Configuration
import ldif.entity.{NodeWritable, EntityDescriptionMetadata, EntityWritable}
import collection.mutable.{HashSet, ArrayBuffer}
import ldif.util.MemoryUsage
import java.util.logging.Logger
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This Reducer builds entities out of the value paths from phases 2 and 3. Under the assumption that all the value paths per entity
 * fit into memory, large entities (with about 1M value paths) can't be constructed.
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
    // all value paths of an entity are needed to build the result table
    // Since Hadoop reuses objects in the iterator they have to be cloned
    var counter = 0
    while(values.hasNext) {
      counter += 1
      if((counter % 10 == 0) && MemoryUsage.getFreeMemoryInBytes()<16777216) {
        System.err.println("PROBLEM: In phase 4. Memory problem for entity " + key.node + ". Number of value paths read: " + counter + ". Used memory: " + Runtime.getRuntime.totalMemory())
        return
      }
      val value = values.next()
      valuePaths.append(WritableUtils.clone(value, config))
//      val valueSet = valuePathsMap.getOrElseUpdate(value.pathID.get(), new HashSet[Array[NodeWritable]])
//      valueSet.add(value.values.get().asInstanceOf[Array[NodeWritable]])
    }

    var (passesRestriction, entityNode) = resultBuilder.checkRestriction(entityDescriptionID, valuePaths)
    if(passesRestriction) {
      var result: IndexedSeq[Traversable[IndexedSeq[NodeWritable]]] = null
      try {
        result = resultBuilder.computeResultTables(entityDescriptionID, valuePaths)
      } catch {
        case e: java.lang.OutOfMemoryError =>
          System.out.println("PROBLEM: In phase 4. Memory problem for entity " + key.node + ". Number of value paths read: " + counter + ". Used memory: " + Runtime.getRuntime.totalMemory())
          return
      }
      if(entityNode==None)
        entityNode = Some(key.node)
      if(hasResults(entityDescriptionID, result)) {
        reporter.incrCounter("LDIF nr. of entities per ED", "ED ID "+key.entityDescriptionID.get(), 1)
        output.collect(key.entityDescriptionID, new EntityWritable(entityNode.get, EntityWritable.convertResultTable(result), key.entityDescriptionID))
        //For Debugging
//        val debugCollector = mos.getCollector("debugReduce", reporter).asInstanceOf[OutputCollector[IntWritable, EntityWritable]]
//        debugCollector.collect(key.entityDescriptionID, new EntityWritable(entityNode.get, EntityWritable.convertResultTable(result), key.entityDescriptionID))
      }
    }
  }

  private def hasResults(entityDescriptionID: Int, results: IndexedSeq[Traversable[IndexedSeq[NodeWritable]]]): Boolean = {
    val entityDescription = edmd.entityDescriptions(entityDescriptionID)
    for(patternIndex <- 0 until entityDescription.patterns.length)
      if(entityDescription.patterns(patternIndex).length==0 || (entityDescription.patterns(patternIndex).length>0 && results(patternIndex).size>0))
        return true
    return false
  }

  override def close() {
    mos.close()
  }
}