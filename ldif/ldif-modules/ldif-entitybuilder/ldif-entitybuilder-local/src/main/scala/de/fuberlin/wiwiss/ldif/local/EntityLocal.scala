package de.fuberlin.wiwiss.ldif.local

import ldif.entity._

class EntityLocal(val uri : String, val graph: String, val entityDescription : EntityDescription, factumBuilder : FactumBuilder) extends Entity {

  override def factums(patternId : Int) =
    factumBuilder.buildFactumTable(uri,entityDescription.patterns(patternId))
}

class FactumTableLocal(table : Traversable[FactumRow]) extends FactumTable {
  def foreach[U](f: FactumRow => U) = util.Random.shuffle(table.toSeq).foreach(f)
}

class FactumRowLocal(row : IndexedSeq[Node]) extends FactumRow {
  override def apply (idx: Int) = row(idx)
  override def length = row.length
}