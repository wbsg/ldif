package ldif.entity

trait Entity extends Serializable
{
  def resource : NodeTrait

//  def entityDescription : EntityDescription

  def factums(patternId : Int) : Traversable[IndexedSeq[Node]]
}



