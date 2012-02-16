/*
 * LDIF
 *
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

package ldif.local

import collection.mutable.{HashMap, MultiMap, Set}
import runtime.impl.QuadQueue
import ldif.runtime.{QuadWriter, Quad}
import ldif.entity.Node
import runtime.{LocalNode, QuadReader}

// Scala MultiMap adapter, including value markers

class MarkedMemHashTable extends HashTable {


  private val hashTable:MultiMap[Pair[Node,String], (Node,Boolean)] = new HashMap[Pair[Node,String], Set[(Node,Boolean)]] with MultiMap[Pair[Node,String], (Node,Boolean)]

  override def getAllQuads(direction : PropertyType.Value = PropertyType.FORW) : QuadReader = {
    val qq = new QuadQueue
    hashTable.foreach(getAllQuads(_, qq, direction))
    qq
  }

  private def getAllQuads(elem : ((Node,String),Set[(Node,Boolean)]), writer : QuadWriter, direction : PropertyType.Value)  {
    val subj = LocalNode.decompress(elem._1._1)
    val prop = elem._1._2
    for ((cObj,marker) <- elem._2)  {
      val obj = LocalNode.decompress(cObj)
      if (direction == PropertyType.FORW)
        writer.write(Quad(subj, prop, obj, obj.graph))
      else
        writer.write(Quad(obj, prop, subj, obj.graph))
    }
  }

  // Retrieve not-used quads using markers
  def getNotUsedQuads(direction : PropertyType.Value = PropertyType.FORW) : QuadReader =  {
    val qq = new QuadQueue
    hashTable.foreach(getNotUsedQuads(_, qq, direction))
    qq
  }

  private def getNotUsedQuads(elem : ((Node,String),Set[(Node,Boolean)]), writer : QuadWriter, direction : PropertyType.Value)  {
    val subj = LocalNode.decompress(elem._1._1)
    val prop = elem._1._2
    for ((cObj,marker) <- elem._2)
      if (marker == false)  {
        val obj = LocalNode.decompress(cObj)
        if (direction == PropertyType.FORW)
          writer.write(Quad(subj, prop, obj, obj.graph))
        else
          writer.write(Quad(obj, prop, subj, obj.graph))
      }
  }

  override def put(key : Pair[Node,String], value: Node) {
    hashTable.addBinding(key,(value, false))
  }

  def put(key : Pair[Node,String], value : (Node, Boolean)) {
    hashTable.addBinding(key, value)
  }

  override def get(key : Pair[Node,String]) = {
    hashTable.get(key) match {
      case Some(mNodes) => {
        hashTable.remove(key)
        for ((node,marker) <- mNodes)
          hashTable.addBinding(key,(node, true))
        Some(mNodes.map(_._1))
      }
      case None => None
    }
  }

  override def clear {
    hashTable.clear
  }

}