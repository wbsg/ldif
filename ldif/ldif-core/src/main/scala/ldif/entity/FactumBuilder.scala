package ldif.entity

trait FactumBuilder{

  def buildFactumTable (resource : Node, pattern : IndexedSeq[Path]) : FactumTable
  
}