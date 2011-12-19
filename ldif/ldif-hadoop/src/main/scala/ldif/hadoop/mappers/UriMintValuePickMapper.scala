/* 
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

package ldif.hadoop.mappers

import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.{Text, NullWritable, IntWritable}
import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.entity.NodeWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */

class UriMintValuePickMapper extends MapReduceBase with Mapper[NullWritable, QuadWritable, NodeWritable, Text] {
  private var mos: MultipleOutputs = null
  private var mintPropertySet: Set[String] = null

  override def configure(conf: JobConf) {
    mintPropertySet = HadoopHelper.getDistributedObject(conf, "mintPropertySet").asInstanceOf[Set[String]]
    mos = new MultipleOutputs(conf)
  }

  /**
   * Collect all values of mint properties for each entity
   */
  override def map(key: NullWritable, quad: QuadWritable, output: OutputCollector[NodeWritable, Text], reporter: Reporter) {
    if(mintPropertySet.contains(quad.property.toString))
      output.collect(quad.subject, new Text(quad.obj.value))
  }

  override def close() {
    mos.close()
  }
}