package ldif.local.runtime

import ldif.entity.{Entity, EntityDescription}

trait CacheReader
{
  val entityDescription : EntityDescription

  def size : Int

  def readEntity(index : Int) : Entity
}
