package ldif.entity

//TODO hide factums implementation
class Entity(val uri : String, val factums : IndexedSeq[Traversable[Factum]], entityDescription : EntityDescription)
{
  //TODO def factum(pathId : Int) : Traversable[Factum]
}