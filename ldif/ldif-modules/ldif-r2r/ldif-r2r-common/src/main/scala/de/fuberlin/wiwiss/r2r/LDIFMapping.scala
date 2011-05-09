package de.fuberlin.wiwiss.r2r

import ldif.entity.Entity

class LDIFMapping(mapping: Mapping) {

}

object LDIFMapping {
  def apply(mapping: Mapping): LDIFMapping = new LDIFMapping(mapping)
}