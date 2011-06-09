package ldif.local.runtime

trait QuadWriter
{
  def write(quad : Quad)
  def finish
}