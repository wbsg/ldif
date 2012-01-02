/* 
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

package ldif.hadoop.mappers

import ldif.hadoop.types.QuadWritable
import ldif.entity.NodeWritable
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.util.Consts
import ldif.hadoop.runtime.RewriteObjectUris

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 12:54 PM
 * To change this template use File | Settings | File Templates.
 */

class UriRewritingMapper extends MapReduceBase with Mapper[NullWritable, QuadWritable, NodeWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private var rewriteObjectNode = false

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
    rewriteObjectNode = HadoopHelper.getDistributedObject(conf,"rewriteObjectUris").asInstanceOf[RewriteObjectUris].value
  }

  /**
   * Collect all values of mint properties for each entity
   */
  override def map(key: NullWritable, quad: QuadWritable, output: OutputCollector[NodeWritable, QuadWritable], reporter: Reporter) {
    var nodeToRewrite: NodeWritable = null
    if(rewriteObjectNode && quad.property.toString!=Consts.SAMEAS_URI)
      nodeToRewrite = quad.obj
    else
      nodeToRewrite = quad.subject
    output.collect(nodeToRewrite, quad)
  }

  override def close() {
    mos.close()
  }
}