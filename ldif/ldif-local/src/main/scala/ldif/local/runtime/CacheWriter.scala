package ldif.local.runtime

import ldif.entity.{Entity, EntityDescription}

trait CacheWriter
{
  val entityDescription : EntityDescription

  def write(resource : Entity)
}