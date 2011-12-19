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
import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.io.{IntWritable, BooleanWritable, Text, NullWritable}
import ldif.entity.EntityWritable
import ldif.modules.silk.LdifEntity

class ConfidenceMap extends Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence] {

  private var linkSpec: LinkSpecification = null

  private var entityDescs: DPair[EntityDescription] = null

  protected override def setup(context: Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence]#Context) {
    linkSpec = Config.readLinkSpec(context.getConfiguration)

    entityDescs = linkSpec.entityDescriptions
  }

  protected override def map(key : BooleanWritable, entities : EntityPairWritable, context : Mapper[BooleanWritable, EntityPairWritable, Text, EntityConfidence]#Context) {
    if(key.get) {
      val confidence = linkSpec.rule(DPair(new LdifEntity(entities.source, entityDescs.source), new LdifEntity(entities.target, entityDescs.target)), 0.0)

      if(confidence >= 0.0) {
        context.write(new Text(entities.source.resource.value), new EntityConfidence(confidence, entities.target.resource.value))
      }
    }
  }
}