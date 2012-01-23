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

import ldif.hadoop.types.{SameAsPairWritable, QuadWritable}
import ldif.datasources.dump.QuadParser
import ldif.util.Consts
import org.apache.hadoop.io._
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.hadoop.utils.URITranslatorHelperMethods
/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:24 PM
 * To change this template use File | Settings | File Templates.
 */

class ExtractSameAsPairsMapper extends MapReduceBase with Mapper[NullWritable, QuadWritable, Text, SameAsPairWritable] {
  private val parser = new QuadParser
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    mos = new MultipleOutputs(conf)
  }

  override def map(key: NullWritable, quad: QuadWritable, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {

    if(quad!=null && quad.property.toString==Consts.SAMEAS_URI) {
      URITranslatorHelperMethods.extractAndOutputSameAsPairs(quad.subject.value, quad.obj.value, output, 1)
      // URITranslatorHelperMethods.extractAndOutputSameAsPairs(quad.subject.value, quad.obj.value, output, 1, mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[Text, SameAsPairWritable]])
      reporter.getCounter("LDIF Stats","Nr. of sameAs links").increment(1)
    }
  }

  override def close() {
    mos.close()
  }
}
