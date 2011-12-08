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

package ldif.hadoop.entitybuilder.mappers

import org.apache.hadoop.mapred._
import ldif.datasources.dump.QuadParser
import lib.MultipleOutputs
import org.apache.hadoop.io._
import ldif.hadoop.types._
import ldif.hadoop.utils.HadoopHelper
import ldif.entity.EntityDescriptionMetadata
import ldif.runtime.Quad
import ldif.util.Consts

class ExtractAndProcessQuadsMapper extends MapReduceBase with Mapper[LongWritable, Text, IntWritable, ValuePathWritable] {
  private val parser = new QuadParser
  private var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def map(key: LongWritable, value: Text, output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    var quad: Quad = null
    try {
      quad = parser.parseLine(value.toString)
    } catch {
      case e: Exception => quad = null
    }

    if(quad==null)
      return
    if(quad.predicate == Consts.SAMEAS_URI)   {
      val collector = mos.getCollector("sameas", reporter).asInstanceOf[OutputCollector[NullWritable, QuadWritable]]
      collector.collect(NullWritable.get, new QuadWritable(quad))
      reporter.getCounter("LDIF Stats","SameAs links from data set").increment(1)
    }
    else
      ProcessQuads.processQuad(quad, reporter, edmd, mos)
  }

  override def close() {
    mos.close()
  }
}
