package ldif.local.runtime

import ldif.entity.{Entity, EntityDescription}

trait CacheReader
{
  val entityDescription : EntityDescription

  val bucketCount : Int

  def readBucket(index : Int) : Array[Entity]
}
