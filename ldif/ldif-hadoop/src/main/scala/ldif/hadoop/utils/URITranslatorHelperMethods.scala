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

package ldif.hadoop.utils

import ldif.hadoop.types.SameAsPairWritable
import ldif.runtime.Quad
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapred.OutputCollector
import ldif.entity.entityComparator.entityComparator

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */

object URITranslatorHelperMethods {
  def extractAndOutputSameAsPairs(subj: String, obj: String, output: OutputCollector[Text, SameAsPairWritable], iteration: Int, debugOutput: OutputCollector[Text, SameAsPairWritable] = null) {
    val sameAsPair = new SameAsPairWritable()
    val uri = new Text()
    val from = subj
    val to = obj

    setSameAsPair(sameAsPair, from, to, iteration)
    uri.set(from)
//    debugOutput.collect(uri, sameAsPair) //DEBUG
    output.collect(uri, sameAsPair)

    setSameAsPair(sameAsPair, to, from, iteration)
    uri.set(to)
//    debugOutput.collect(uri, sameAsPair) //DEBUG
    output.collect(uri, sameAsPair)
  }

  private def setSameAsPair(sameAsPair: SameAsPairWritable, from: String, to: String, iteration: Int): SameAsPairWritable = {
    sameAsPair.from = from
    sameAsPair.to = to
    sameAsPair.iteration = iteration
    return sameAsPair
  }

  def simpleCompare(left: String, right: String): Boolean = {
    entityComparator.lessThan(left, right)
  }
}