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

package ldif.modules.r2r.local

import ldif.local.runtime.impl.EntityQueue
import collection.mutable.HashSet
import ldif.entity._
import de.fuberlin.wiwiss.r2r.{Repository, LDIFMapping}
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 19.05.11
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */

object CreatorHelperFunctions {
  def createEntityQueue(entityDescription: EntityDescription): EntityQueue = {
    new EntityQueue(entityDescription)
  }

  def addEntityToEntityQueue(entity: Entity, entityQueue: EntityQueue) {
    entityQueue.write(entity)
  }

  def createEntity(entityUri: String, mapping: LDIFMapping): MutableEntity = {
    new MutableEntity(Node.fromString(entityUri, Consts.DEFAULT_GRAPH), mapping)
  }

  def getMapping(mappingURI: String, repository: Repository): LDIFMapping = {
    LDIFMapping(repository.getMappingOfUri(mappingURI))
  }
}

class MutableEntity(val resource : Node, mapping: LDIFMapping) extends Entity {
  val resultTable = new HashSet[FactumRow] with FactumTable
  override def factums(patternID: Int, factumBuilder : FactumBuilder = null) : FactumTable = resultTable
  def entityDescription = mapping.entityDescription
  def addFactumRow(nodes: Node*) {
    val row = new FactumRow {
      def length = nodes.length
      def apply(idx: Int) = nodes(idx)
    }
    resultTable.add(row)
  }
}