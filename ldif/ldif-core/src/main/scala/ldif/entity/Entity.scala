package ldif.entity

class Entity(val uri : String, factums : IndexedSeq[Traversable[Factum]], entityDescription : EntityDescription)
{
  def factum(pathId : Int) : Traversable[Factum] = factums(pathId)
}