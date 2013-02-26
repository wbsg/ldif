/*
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk.hadoop

import io.{PartitionWritable, IndexedEntityWritable}
import org.apache.hadoop.mapred.Reducer
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import ldif.modules.silk.LdifEntity
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.{EntityDescription, Entity}
import de.fuberlin.wiwiss.silk.cache.{Partition, BitsetIndex}
import org.apache.hadoop.io.{Text, IntWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, JobConf, MapReduceBase}

class IndexReduce extends MapReduceBase 
                  with Reducer[IntWritable, IndexedEntityWritable, IntWritable, PartitionWritable]
                  with Configured {

  val partitionSize = 10000

  protected override def reduce(index : IntWritable,
                                iterator : java.util.Iterator[IndexedEntityWritable],
                                collector: OutputCollector[IntWritable, PartitionWritable],
                                reporter: Reporter) {
    var currentEntities = new Array[Entity](partitionSize)
    var currentIndices = new Array[BitsetIndex](partitionSize)
    var count = 0

    while(iterator.hasNext) {
      val next = iterator.next()
      currentEntities(count) = new LdifEntity(next.entity, entityDescs.select(isSource))
      currentIndices(count) = next.index
      count += 1
      if(count == partitionSize) {
        count = 0
        collector.collect(index, new PartitionWritable(Partition(currentEntities, currentIndices)))
        currentEntities = new Array[Entity](partitionSize)
        currentIndices = new Array[BitsetIndex](partitionSize)
      }
    }
    
    if(count > 0) {
      collector.collect(index, new PartitionWritable(Partition(currentEntities, currentIndices, count)))
    }
  }
}