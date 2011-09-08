package ldif.local.runtime

import ldif.runtime.Quad

trait QuadReader
{
  def size : Int
  def read() : Quad
  def hasNext : Boolean
}

