/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.modules.r2r.hadoop

import ldif.entity.EntityWritable
import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.{NullWritable, IntWritable}
import org.apache.hadoop.mapred.{Reporter, OutputCollector, Mapper, MapReduceBase}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */

class R2RMapper extends MapReduceBase with Mapper[IntWritable, EntityWritable, NullWritable, QuadWritable] {


  def map(key: IntWritable, value: EntityWritable, collector: OutputCollector[NullWritable, QuadWritable], reporter: Reporter) {

  }
}