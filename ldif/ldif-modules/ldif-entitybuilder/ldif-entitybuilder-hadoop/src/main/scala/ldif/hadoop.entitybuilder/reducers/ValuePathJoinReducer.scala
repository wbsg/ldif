/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.hadoop.entitybuilder.reducers

import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import collection.mutable.ArrayBuffer
import java.util.Iterator
import ldif.hadoop.types._
import ldif.entity.EntityDescriptionMetadata
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{WritableUtils, IntWritable, Writable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/25/11
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */

class ValuePathJoinReducer extends MapReduceBase with Reducer[PathJoinValueWritable, ValuePathWritable, IntWritable, ValuePathWritable] {
  var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null
  private val pathIDWritable = new IntWritable
  private val nodeValues = new NodeArrayWritable
  private var collector: OutputCollector[IntWritable, ValuePathWritable] = null
  private var config: Configuration = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
    config = conf
  }

  override def reduce(key: PathJoinValueWritable, values: Iterator[ValuePathWritable], output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    val entityPaths = new ArrayBuffer[Array[Writable]]()
    val joinPaths = new ArrayBuffer[Writable]()
    val pathID = key.pathID.get
    pathIDWritable.set(pathID)
    val finalPathLength = edmd.pathLength(pathID)

    while(values.hasNext) {
      val value = WritableUtils.clone(values.next(), config)
      value.pathType match {
        case EntityPathType => entityPaths.append(value.values.get); reporter.getCounter("LDIF stats", "reducer: entity paths received").increment(1)
        case JoinPathType => joinPaths.append(value.values.get()(1)); reporter.getCounter("LDIF stats", "reducer: join paths received").increment(1)
        case _ => throw new RuntimeException("Cannot join paths with type: " + value.pathType)
      }
    }

    for(entityPath <- entityPaths) {
      val nextPhase = entityPath.length - 2
      for(joinPath <- joinPaths) {
        nodeValues.set((entityPath ++ List(joinPath)).toArray)
        val pathType = {
          if(nodeValues.get.length-1 == finalPathLength)
            FinishedPathType
          else
            EntityPathType
        }
        val path = new ValuePathWritable(pathIDWritable, pathType, nodeValues)
//        if(pathType==FinishedPathType)
//          reporter.getCounter("LDIF stats", "Nr. of finished paths output-r").increment(1)
//        else
//          reporter.getCounter("LDIF stats", "Nr. of entity paths output-r").increment(1)
        collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
        collector.collect(new IntWritable(nextPhase), path)
        // Debugging
//        collector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//        collector.collect(new IntWritable(nextPhase), path)
      }
    }
  }

  override def close() {
    mos.close()
  }
}