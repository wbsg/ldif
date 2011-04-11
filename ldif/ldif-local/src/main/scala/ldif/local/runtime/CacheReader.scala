package ldif.local.runtime

import ldif.resource.{Resource, ResourceFormat}

trait CacheReader
{
  val resourceFormat : ResourceFormat

  val bucketCount : Int

  def readBucket(index : Int) : Array[Resource]
}
