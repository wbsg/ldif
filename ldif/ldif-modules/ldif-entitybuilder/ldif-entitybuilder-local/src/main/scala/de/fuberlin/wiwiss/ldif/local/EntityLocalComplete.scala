package de.fuberlin.wiwiss.ldif.local

import ldif.entity._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 01.07.11
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */

case class EntityLocalComplete(val uri : String, val graph: String, val entityDescription : EntityDescription, resultTable: IndexedSeq[Traversable[IndexedSeq[Node]]]) extends Entity {
  def factums(patternId: Int) = resultTable(patternId)

}