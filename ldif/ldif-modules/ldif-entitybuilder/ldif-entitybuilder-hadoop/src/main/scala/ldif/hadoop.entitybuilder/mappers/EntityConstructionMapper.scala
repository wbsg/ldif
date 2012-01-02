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

package ldif.hadoop.entitybuilder.mappers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types._
import ldif.entity.{EntityDescriptionMetadata, NodeWritable}
import ldif.hadoop.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/27/11
 * Time: 12:22 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityConstructionMapper extends MapReduceBase with Mapper[IntWritable, ValuePathWritable, EntityDescriptionNodeWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  val entityDescriptionID = new IntWritable()
  val entityDescriptionNode = new EntityDescriptionNodeWritable()
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def map(key: IntWritable, value: ValuePathWritable, output: OutputCollector[EntityDescriptionNodeWritable, ValuePathWritable], reporter: Reporter) {
    entityDescriptionID.set(edmd.pathMap(value.pathID.get).entityDescriptionIndex)
    entityDescriptionNode.set(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable])
    // Debugging
//    val debugCollector = mos.getCollector("debugMap", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//    debugCollector.collect(entityDescriptionID, value)
    output.collect(new EntityDescriptionNodeWritable(entityDescriptionID, value.values.get()(0).asInstanceOf[NodeWritable]), value)
  }

  override def close() {
    mos.close()
  }
}