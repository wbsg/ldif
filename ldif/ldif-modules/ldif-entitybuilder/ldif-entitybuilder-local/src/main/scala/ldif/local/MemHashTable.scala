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

package ldif.local

import collection.mutable.{HashMap, MultiMap, Set}
import runtime.impl.QuadQueue
import ldif.entity.Node
import runtime.LocalNode
import ldif.runtime.{QuadReader, Quad, QuadWriter}

// Scala MultiMap adapter

class MemHashTable extends HashTable {

  protected val hashTable:MultiMap[Pair[Node,String], Node] = new HashMap[Pair[Node,String], Set[Node]] with MultiMap[Pair[Node,String], Node]

  override def put(key : Pair[Node,String], value: Node) {
    hashTable.addBinding(key,value)
  }

  override def get(key : Pair[Node,String]) = {
    hashTable.get(key)
  }

  override def clear {
    hashTable.clear
  }

  override def getAllQuads(direction : PropertyType.Value = PropertyType.FORW) : QuadReader = {
    val qq = new QuadQueue
    hashTable.foreach(getAllQuads(_, qq, direction))
    qq
  }

  private def getAllQuads(elem : ((Node,String),Set[Node]), writer : QuadWriter, direction : PropertyType.Value) : Unit = {
    val subj = LocalNode.decompress(elem._1._1)
    val prop = elem._1._2
    for (cObj <- elem._2)  {
      val obj = LocalNode.decompress(cObj)
      if (direction == PropertyType.FORW)
        writer.write(Quad(subj, prop, obj, obj.graph))
      else
        writer.write(Quad(obj, prop, subj, obj.graph))
    }
  }

}