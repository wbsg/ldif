package ldif.entity

class EntityLocal(val resource : Node, val entityDescription : EntityDescription) extends Entity with Serializable {

  override def factums(patternId : Int): Traversable[IndexedSeq[Node]] =
    EntityLocalMetadata.factumBuilder.buildFactumTable(resource,entityDescription.patterns(patternId))
}

class FactumTableLocal(table : Traversable[FactumRow]) extends FactumTable {
  def foreach[U](f: FactumRow => U) = util.Random.shuffle(table.toSeq).foreach(f)
}

class FactumRowLocal(row : IndexedSeq[Node]) extends FactumRow {
  override def apply (idx: Int) = row(idx)
  override def length = row.length
}

object EntityLocalMetadata {
  var factumBuilder: FactumBuilder = null
}