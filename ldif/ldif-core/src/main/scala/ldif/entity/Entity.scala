package ldif.entity

trait Entity
{
  def uri : String

  def entityDescription : EntityDescription

  def factums(patternId : Int) : FactumTable
}

//Each pattern has a table of values (=Factums)
// one column for each path in the pattern
// order is not important
trait FactumTable extends Traversable[FactumRow]

//One value (=Factum) for each path in the pattern
// order here is important
trait FactumRow extends IndexedSeq[Node]

