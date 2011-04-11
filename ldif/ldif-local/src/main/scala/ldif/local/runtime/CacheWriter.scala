package ldif.local.runtime

import ldif.resource.{Resource, ResourceFormat}

trait CacheWriter
{
  val resourceFormat : ResourceFormat

  def write(resource : Resource)
}