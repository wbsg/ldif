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

package ldif.hadoop.reducers

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.hadoop.utils.HadoopHelper
import java.util.Iterator
import ldif.entity.entityComparator.entityComparator
import org.apache.hadoop.io.{Text, NullWritable}
import ldif.hadoop.types.QuadWritable
import ldif.entity.{Node, NodeWritable}
import ldif.util.{Consts, UriMintHelper}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */

class UriMintValuePickerReducer extends MapReduceBase with Reducer[NodeWritable, Text, NullWritable, QuadWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null
  private var mintNamespace: String = null
  private val sameAsQuad = new QuadWritable(null, new Text(Consts.SAMEAS_URI), null, new Text(Consts.URI_MINTING_GRAPH))

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
    mintNamespace = HadoopHelper.getDistributedObject(conf, "mintNamespace").asInstanceOf[String]
  }

  override def reduce(entity: NodeWritable, values : Iterator[Text], output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    var max = values.next().toString
    while(values.hasNext) {
      val value = values.next().toString
      if(entityComparator.lessThan(max, value))
        max = value
    }
    val mintedNode = new NodeWritable(UriMintHelper.mintURI(mintNamespace, max), null, Node.UriNode, entity.graph)
    sameAsQuad.subject = entity
    sameAsQuad.obj = mintedNode
    output.collect(NullWritable.get(), sameAsQuad)
  }

  override def close() {
    mos.close()
  }
}