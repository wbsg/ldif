package ldif.entity

trait Entity
{
  def uri : String

  def entityDescription : EntityDescription

  def factums(patternId : Int) : FactumTable
}



