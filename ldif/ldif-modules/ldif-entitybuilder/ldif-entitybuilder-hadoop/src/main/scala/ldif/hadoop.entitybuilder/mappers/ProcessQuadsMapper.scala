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

package ldif.hadoop.entitybuilder.mappers

import org.apache.hadoop.mapred.lib.MultipleOutputs
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.mapred._
import ldif.hadoop.types._
import ldif.entity.{NodeTrait, NodeWritable, EntityDescriptionMetadata}
import ldif.runtime.Quad
import org.apache.hadoop.io._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/6/11
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */

class ProcessQuadsMapper extends MapReduceBase with Mapper[NullWritable, QuadWritable, IntWritable, ValuePathWritable] {
  private var edmd: EntityDescriptionMetadata = null
  private var mos: MultipleOutputs = null

  override def configure(conf: JobConf) {
    edmd = HadoopHelper.getEntityDescriptionMetaData(conf)
    mos = new MultipleOutputs(conf)
  }

  override def map(nothing: NullWritable, quad: QuadWritable, output: OutputCollector[IntWritable, ValuePathWritable], reporter: Reporter) {
    reporter.getCounter("LDIF Stats","Valid triples/quads found in data set").increment(1)
    ProcessQuads.processQuad(quad.asQuad, reporter, edmd, mos)
  }

  override def close() {
    mos.close()
  }
}

object ProcessQuads {
  def processQuad(quad: Quad, reporter: Reporter, edmd: EntityDescriptionMetadata, mos: MultipleOutputs) {
    val property = quad.predicate
    val values = new NodeArrayWritable
    val phase = new IntWritable(0)
    val propertyInfosValue = edmd.propertyMap.get(property)
    propertyInfosValue match {
      case None => {
        reporter.getCounter("LDIF Stats","Nr. of irrelevant quads filtered").increment(1)
      }
      case Some(propertyInfos) =>
        reporter.getCounter("LDIF Stats","Nr. of potentially relevant quads").increment(1)
        for (propertyInfo <- propertyInfos) {
          val pathLength = edmd.pathLength(propertyInfo.pathId)
          val pathType = {
            if (pathLength == 1)
              FinishedPathType
            else if (propertyInfo.phase == 0) EntityPathType
            else JoinPathType
          }
          val subj = new NodeWritable(quad.subject)
          val obj = new NodeWritable((quad.value))
          if (propertyInfo.isForward)
            values.set(Array[Writable](subj, obj))
          else
            values.set(Array[Writable](obj, subj))

          if (pathType != FinishedPathType)
            phase.set(propertyInfo.phase)

          val path = new ValuePathWritable(new IntWritable(propertyInfo.pathId), pathType, values)
          // Do not collect restricted paths that don't contain one of the restriction values
          if (pathLength - 1 != phase.get()
            || (propertyInfo.restrictionValues == None
            || propertyInfo.restrictionValues.get.contains(values.get()(1).asInstanceOf[NodeTrait]))) {
            reporter.getCounter("LDIF Stats","Nr. of value paths output").increment(1)
            val collector = mos.getCollector("seq", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
            collector.collect(phase, path)
            // For debugging
//            val tCollector = mos.getCollector("text", reporter).asInstanceOf[OutputCollector[IntWritable, ValuePathWritable]]
//            tCollector.collect(phase, path)
          }
        }
    }
  }
}
