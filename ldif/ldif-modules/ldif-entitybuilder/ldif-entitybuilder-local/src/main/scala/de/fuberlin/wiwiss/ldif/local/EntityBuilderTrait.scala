package de.fuberlin.wiwiss.ldif.local

import ldif.entity.EntityDescription
import ldif.local.runtime.EntityWriter

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 07.07.11
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */

trait EntityBuilderTrait {
  def buildEntities (ed : EntityDescription, writer : EntityWriter)
}