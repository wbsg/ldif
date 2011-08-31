package ldif.local.runtime

trait QuadReader
{
  def size : Int
  def read() : Quad
  def hasNext : Boolean
}

