/* 
 * LDIF
 *
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

package ldif.local

import ldif.entity.Node
import collection.mutable.{HashMap, MultiMap, Set}

// Scala MultiMap adapter

class MemHashTable extends HashTable {

  val hashTable:MultiMap[Pair[Node,String], Node] = new HashMap[Pair[Node,String], Set[Node]] with MultiMap[Pair[Node,String], Node]

  override def put(key : Pair[Node,String], value: Node) {
    hashTable.addBinding(key,value)
  }

  override def get(key : Pair[Node,String]) = {
    hashTable.get(key)
  }

  override def clear {
    hashTable.clear
  }

}