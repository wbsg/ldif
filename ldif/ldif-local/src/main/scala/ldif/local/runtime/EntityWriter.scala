package ldif.local.runtime

import ldif.entity.{EntityDescription, Entity}

trait EntityWriter
{
  val entityDescription : EntityDescription
  def write(entity : Entity)
}