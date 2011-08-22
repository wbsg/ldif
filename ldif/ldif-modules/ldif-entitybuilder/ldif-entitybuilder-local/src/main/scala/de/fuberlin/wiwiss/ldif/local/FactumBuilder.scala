package de.fuberlin.wiwiss.ldif.local

import ldif.entity.{Node, Path, FactumTable}

trait FactumBuilder{

  def buildFactumTable (resource : Node, pattern : IndexedSeq[Path]) : FactumTable
  
}