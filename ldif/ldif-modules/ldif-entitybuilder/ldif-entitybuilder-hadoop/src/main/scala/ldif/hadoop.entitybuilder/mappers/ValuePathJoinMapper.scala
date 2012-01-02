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

import org.apache.hadoop.io.{IntWritable}
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import ldif.hadoop.types._
import ldif.entity.{EntityDescriptionMetadata, NodeWritable}
import ldif.hadoop.utils.HadoopHelper

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:32 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathJoinMapper extends MapReduceBase with Mapper[IntWritable, ValuePathWritable, PathJoinValueWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null
  private var collector: OutputCollector[IntWritable, ValuePathWritable] = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  def map(key: IntWritable, value: ValuePathWritable, output: OutputCollector[PathJoinValueWritable, ValuePathWritable], reporter: Reporter) {
    if(value.pathType==FinishedPathType) {
//      reporter.getCounter("LDIF stats", "Nr. of finished paths output-m").increment(1)
      collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
      collector.collect(key, value)
      // Debugging
//      collector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//      collector.collect(key, value)
    }
    else {
      val nodes = value.values.get
      if(value.pathType==EntityPathType) {
//        reporter.getCounter("LDIF stats", "Nr. of entity paths output-m").increment(1)
        output.collect(new PathJoinValueWritable(value.pathID, nodes(nodes.length-1).asInstanceOf[NodeWritable]), value)
      }
      else {
//        reporter.getCounter("LDIF stats", "Nr. of join paths output-m").increment(1)
        output.collect(new PathJoinValueWritable(value.pathID, nodes(0).asInstanceOf[NodeWritable]), value)
      }
    }
  }

  override def close() {
    mos.close()
  }
}