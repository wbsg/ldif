package ldif.entity

trait Entity
{
  def uri : String

  def entityDescription : EntityDescription

  def factum(patternId : Int) : FactumTable
}

trait FactumTable extends IndexedSeq[FactumRow]
{

}

trait FactumRow extends IndexedSeq[Factum]
{
}
