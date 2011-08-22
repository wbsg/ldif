package ldif.entity

trait Entity extends Serializable
{
  def resource : Node

  def entityDescription : EntityDescription

  def factums(patternId : Int) : Traversable[IndexedSeq[Node]]
}



