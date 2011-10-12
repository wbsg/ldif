package de.fuberlin.wiwiss.ldif.local

import ldif.entity.EntityDescription
import ldif.local.runtime.EntityWriter

trait EntityBuilderTrait {
  def buildEntities (ed : EntityDescription, writer : EntityWriter)
}