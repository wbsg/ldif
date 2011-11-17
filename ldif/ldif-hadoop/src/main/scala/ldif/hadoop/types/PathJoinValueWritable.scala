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

package ldif.hadoop.types

import java.io.{DataInput, DataOutput}
import ldif.entity.NodeWritable
import org.apache.hadoop.io.{IntWritable, WritableComparable}

class PathJoinValueWritable (var pathID : IntWritable, var node : NodeWritable) extends WritableComparable[PathJoinValueWritable]{
  def this() {this(new IntWritable(), new NodeWritable())}

  def compareTo(other: PathJoinValueWritable) = {
    if(pathID.compareTo(other.pathID)==0)
      node.compareTo(other.node)
    else
      pathID.compareTo(other.pathID)
  }

  def readFields(input: DataInput) {
    pathID.readFields(input)
    node.readFields(input)
  }

  def write(output: DataOutput) {
    pathID.write(output)
    node.write(output)
  }
}