/* 
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

import ldif.hadoop.types.SameAsPairWritable
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.mapred.lib.MultipleOutputs
import org.apache.hadoop.mapred._
import ldif.hadoop.utils.URITranslatorHelperMethods

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/30/11
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */

class WriteRemainingSameAsPairsMapper extends MapReduceBase with Mapper[NullWritable, SameAsPairWritable, Text, SameAsPairWritable] {
  override def map(key: NullWritable, sameAsPair: SameAsPairWritable, output: OutputCollector[Text, SameAsPairWritable], reporter: Reporter) {
    output.collect(new Text(sameAsPair.to), sameAsPair)
  }
}