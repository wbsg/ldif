package ldif.local.runtime

trait QuadReader
{
  //TODO consider hasNext and getNext methods
  def size : Int
  def read : Quad
}
