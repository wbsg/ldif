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

package ldif.entity

class EntityLocal(val resource : Node, val entityDescription : EntityDescription) extends Entity with Serializable {

  override def factums(patternId : Int, factumBuilder : FactumBuilder): Traversable[IndexedSeq[Node]] = {
    if (patternId < entityDescription.patterns.size) {
      factumBuilder.buildFactumTable(resource,entityDescription.patterns(patternId))
    } else {
      Seq[IndexedSeq[Node]]()
    }
  }
}

class FactumTableLocal(table : Traversable[FactumRow]) extends FactumTable {
  def foreach[U](f: FactumRow => U) = util.Random.shuffle(table.toSeq).foreach(f)
}

class FactumRowLocal(row : IndexedSeq[Node]) extends FactumRow {
  override def apply (idx: Int) = row(idx)
  override def length = row.length
}
