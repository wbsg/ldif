package de.fuberlin.wiwiss.ldif.local

import ldif.entity.{Path, FactumTable}

trait FactumBuilder{

  def buildFactumTable (entityUri : String, pattern : IndexedSeq[Path]) : FactumTable
  
}