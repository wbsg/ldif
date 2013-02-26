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
package ldif.local

import collection.mutable.{HashMap, MultiMap, Set}
import ldif.entity.Node

// Scala MultiMap adapter
// - Elements/keys can be read only once
// - Used for collecting not used quads (eg for Fusion)

class MemHashTableReadOnce extends MemHashTable {

  override def get(key : Pair[Node,String]) = {
    val values = hashTable.get(key)
    hashTable.remove(key)
    values
  }

  def getNotUsedQuads (direction : PropertyType.Value = PropertyType.FORW) = getAllQuads(direction)
}