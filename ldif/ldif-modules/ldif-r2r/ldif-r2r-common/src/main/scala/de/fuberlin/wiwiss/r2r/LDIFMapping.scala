package de.fuberlin.wiwiss.r2r

import ldif.entity.Entity

class LDIFMapping(uri: String) extends Mapping(uri) {

}

object LDIFMapping {
  def apply(uri: String): LDIFMapping = new LDIFMapping(uri)
}