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

import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import de.fuberlin.wiwiss.silk.util.DPair
import io.PartitionPairWritable
import org.apache.hadoop.io.{BooleanWritable, Text}
import org.apache.hadoop.mapred._

class MatchMap extends MapReduceBase
               with Mapper[BooleanWritable, PartitionPairWritable, Text, EntityConfidence]
               with Configured {

  protected override def map(key: BooleanWritable,
                             partitions: PartitionPairWritable,
                             collector: OutputCollector[Text, EntityConfidence],
                             reporter: Reporter) {
    if(key.get) {
      val sourcePartition = partitions.source.get
      val targetPartition = partitions.target.get
      if(sourcePartition==null || targetPartition==null)
        return

      //Iterate over all entities in the source partition
      var s = 0
      while(s < sourcePartition.size) {
        //Iterate over all entities in the target partition
        var t = 0
        while(t < targetPartition.size) {
          //Check if the indices match
          if(sourcePartition.indices(s) matches targetPartition.indices(t)) {
            val sourceEntity = sourcePartition.entities(s)
            val targetEntity = targetPartition.entities(t)
            if(sourceEntity.uri!=targetEntity.uri) {
              val entities = DPair(sourceEntity, targetEntity)
              val confidence = linkSpec.rule(entities, 0.0)

              if (confidence >= 0.0) {
                collector.collect(new Text(sourceEntity.uri), new EntityConfidence(confidence, targetEntity.uri))
              }
            }
          }
          t += 1
        }
        s += 1
      }
    }
  }
}