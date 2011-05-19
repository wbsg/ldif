package ldif.modules.r2r.local

import ldif.local.runtime.impl.EntityQueue
import collection.mutable.HashSet
import ldif.entity._
import de.fuberlin.wiwiss.r2r.{Repository, LDIFMapping}
import de.fuberlin.wiwiss.r2r.LDIFMapping._

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
    new MutableEntity(entityUri, mapping)
  }

  def getMapping(mappingURI: String, repository: Repository): LDIFMapping = {
    LDIFMapping(repository.getMappingOfUri(mappingURI))
  }
}

class MutableEntity(entityUri: String, mapping: LDIFMapping) extends Entity {
  val uri = entityUri
  val resultTable = new HashSet[FactumRow] with FactumTable
  override def factums(patternID: Int) : FactumTable = resultTable
  def entityDescription = mapping.entityDescription
  def addFactumRow(nodes: Node*) {
    val row = new FactumRow {
      def length = nodes.length
      def apply(idx: Int) = nodes(idx)
    }
    resultTable.add(row)
  }
}