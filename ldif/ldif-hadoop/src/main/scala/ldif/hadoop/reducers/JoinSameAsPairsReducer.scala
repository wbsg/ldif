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

package ldif.hadoop.reducers

import ldif.hadoop.types.SameAsPairWritable
import scala.collection.mutable.HashSet
import java.util.Iterator
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.io.{Text, WritableUtils, NullWritable}
import ldif.hadoop.utils.URITranslatorHelperMethods
/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/28/11
 * Time: 5:13 PM
 * To change this template use File | Settings | File Templates.
 */

class JoinSameAsPairsReducer extends MapReduceBase with Reducer[Text, SameAsPairWritable, NullWritable, SameAsPairWritable] {
  private var config: Configuration = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    config = conf
    mos = new MultipleOutputs(conf)
  }

  override def reduce(joinEntity: Text, sameAsPairs : Iterator[SameAsPairWritable], output: OutputCollector[NullWritable, SameAsPairWritable], reporter: Reporter) {
    val sameAsPairSet = new HashSet[SameAsPairWritable]()
    var toMax: String = joinEntity.toString

    // Remove duplicates and find largest member of cluster
    while(sameAsPairs.hasNext) {
      val sameAsPair = sameAsPairs.next()
      sameAsPairSet.add(WritableUtils.clone[SameAsPairWritable](sameAsPair,config))
      if(URITranslatorHelperMethods.simpleCompare(toMax, sameAsPair.to))
        toMax = sameAsPair.to
    }

//    val debugOutput = mos.getCollector("debug", reporter).asInstanceOf[OutputCollector[Text, SameAsPairWritable]]
    // Join and write out sameAsPairs
    for(sameAsPair <- sameAsPairSet)
      if(URITranslatorHelperMethods.simpleCompare(sameAsPair.to, toMax)){
        sameAsPair.from = sameAsPair.to
        sameAsPair.to = toMax
        if(!URITranslatorHelperMethods.simpleCompare(joinEntity.toString, toMax) && sameAsPairSet.size < sameAsPair.iteration) {
          val finishedOutput = mos.getCollector("finished", reporter).asInstanceOf[OutputCollector[NullWritable, SameAsPairWritable]]
          finishedOutput.collect(NullWritable.get(), sameAsPair)
        } else {
          output.collect(NullWritable.get(), sameAsPair)
//          debugOutput.collect(new Text(""), sameAsPair)
        }
      }
    // The cluster size is the number of entities in the cluster
    if(!URITranslatorHelperMethods.simpleCompare(joinEntity.toString, toMax))
      reporter.incrCounter("Cluster number by size", counterStringGenerator.generate(sameAsPairSet.size+1).toString, 1)
  }

  override def close() {
    mos.close()
  }
}

object counterStringGenerator {
  def generate(size: Int): String = {
    val sizeString = size.toString
    val padding = map.get(6-sizeString.length()).getOrElse("")
    return padding + sizeString
  }

  private final val map = Map(0->"", 1->" ", 2->"  ", 3->"   ", 4->"    ", 5->"     ", 6->"      ")
}