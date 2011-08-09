package ldif.datasources.sparql

import ldif.module.ModuleConfig
import ldif.entity.EntityDescription

case class SparqlConfig (endpointUrls : Traversable[String], val entityDescriptions : IndexedSeq[EntityDescription]) extends ModuleConfig


