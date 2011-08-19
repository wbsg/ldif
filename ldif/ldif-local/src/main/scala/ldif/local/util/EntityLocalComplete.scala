package ldif.local.util

import ldif.entity._

case class EntityLocalComplete(val uri : String, val graph: String, val entityDescription : EntityDescription, resultTable: IndexedSeq[Traversable[IndexedSeq[Node]]]) extends Entity {
  def factums(patternId: Int) = resultTable(patternId)

}