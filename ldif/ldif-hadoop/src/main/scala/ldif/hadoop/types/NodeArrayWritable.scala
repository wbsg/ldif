/* 
 * LDIF
 *
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

package ldif.hadoop.types

import org.apache.hadoop.io.ArrayWritable
import ldif.entity.NodeWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

class NodeArrayWritable extends ArrayWritable(classOf[NodeWritable]) {

  override def toString = {
    val builder = new StringBuilder
    builder.append("NodeArrayWritable(")
    var notfirst = false
    for(nodeString <- toStrings) {
      if(notfirst)
        builder.append(", ")
      else
        notfirst = true
      builder.append(nodeString)
    }
    builder.append(")")
    builder.toString()
  }
}