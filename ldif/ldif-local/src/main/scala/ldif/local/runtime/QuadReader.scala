package ldif.local.runtime

trait QuadReader
{
  def size : Int
  def isEmpty : Boolean
  def read() : Quad        
  def hasNext : Boolean
}

