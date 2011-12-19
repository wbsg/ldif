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

import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.util.Consts
import ldif.hadoop.types.{QuadWritable, SameAsPairWritable}
import ldif.entity.{Node, NodeWritable}
import org.apache.hadoop.io.{Text, NullWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/1/11
 * Time: 6:29 PM
 * To change this template use File | Settings | File Templates.
 */

class ConvertSameAsPairsToQuadsMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, NullWritable, QuadWritable] {
  private var mos: MultipleOutputs = null
  private val subj = new NodeWritable(null, null, Node.UriNode, "")
  private val obj = new NodeWritable(null, null, Node.UriNode, "")
  private val quad = new QuadWritable(subj, new Text(Consts.SAMEAS_URI), obj, new Text(Consts.URI_REWRITING_GRAPH))

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def close() {
    mos.close()
  }

  override def map(nothing: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {
    subj.value = sameAsPair.from
    obj.value = sameAsPair.to
    output.collect(NullWritable.get(), quad)
  }
}
