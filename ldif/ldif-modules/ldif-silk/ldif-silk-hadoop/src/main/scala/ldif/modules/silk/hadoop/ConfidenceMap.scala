/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.apache.hadoop.mapreduce.Mapper
import de.fuberlin.wiwiss.silk.hadoop.impl.EntityConfidence
import de.fuberlin.wiwiss.silk.config.LinkSpecification
import org.apache.hadoop.io.{BooleanWritable, Text}
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import de.fuberlin.wiwiss.silk.util.{Timer, DPair}

class ConfidenceMap extends Mapper[BooleanWritable, PartitionPairWritable, Text, EntityConfidence] {

  private var linkSpec: LinkSpecification = null

  private var entityDescs: DPair[EntityDescription] = null

  protected override def setup(context: Mapper[BooleanWritable, PartitionPairWritable, Text, EntityConfidence]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)

    entityDescs = linkSpec.entityDescriptions
  }

  protected override def map(key: BooleanWritable,
                             partitions: PartitionPairWritable,
                             context: Mapper[BooleanWritable, PartitionPairWritable, Text, EntityConfidence]#Context) {
    if(key.get) {
      val sourcePartition = partitions.source.get
      val targetPartition = partitions.target.get

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
            val entities = DPair(sourceEntity, targetEntity)
            val confidence = linkSpec.rule(entities, 0.0)

            if (confidence >= 0.0) {
              context.write(new Text(sourceEntity.uri), new EntityConfidence(confidence, targetEntity.uri))
            }
          }
          t += 1
        }
        s += 1
      }
    }
  }
}