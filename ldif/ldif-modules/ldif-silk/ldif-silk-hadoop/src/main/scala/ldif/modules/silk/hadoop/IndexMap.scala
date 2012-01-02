/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import de.fuberlin.wiwiss.silk.config.LinkSpecification
import de.fuberlin.wiwiss.silk.util.DPair
import io.IndexedEntityWritable
import ldif.modules.silk.LdifEntity
import ldif.entity.{EntityWritable}
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.io.IntWritable
import de.fuberlin.wiwiss.silk.cache.BitsetIndex
import org.apache.hadoop.mapred._

class IndexMap extends MapReduceBase with Mapper[IntWritable, EntityWritable, IntWritable, IndexedEntityWritable] {

  val blockCount = 1000

  private var linkSpec: LinkSpecification = null
  
  private var entityDescs: DPair[EntityDescription] = null
  
  protected override def configure(conf: JobConf) {
    linkSpec = Config.readLinkSpec(conf)
    
    entityDescs = linkSpec.entityDescriptions
  }
  
  protected override def map(key: IntWritable,
                             entity: EntityWritable,
                             collector: OutputCollector[IntWritable, IndexedEntityWritable],
                             reporter: Reporter) {
    
    val index = linkSpec.rule.index(new LdifEntity(entity, entityDescs.select(key.get % 2 == 0)))

    for((block, i) <- index.flatten.groupBy(i => math.abs(i % blockCount))) {
      collector.collect(new IntWritable(block), new IndexedEntityWritable(entity, BitsetIndex.build(i)))
    }
  }
}