package ldif.local.runtime

import ldif.runtime.Quad

trait QuadWriter
{
  def write(quad : Quad)
  def finish
}