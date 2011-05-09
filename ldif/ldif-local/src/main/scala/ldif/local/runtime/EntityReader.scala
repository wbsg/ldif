package ldif.local.runtime

import ldif.entity.{EntityDescription, Entity}

trait EntityReader
{
  val entityDescription : EntityDescription
  def size : Int
  def isEmpty : Boolean
  def read() : Entity
}