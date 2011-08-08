package ldif.datasources.sparql

import ldif.module.ModuleTask
import ldif.util.Identifier
import ldif.entity.EntityDescription

class SparqlTask(override val name : Identifier, val endpointUrl : String, val entityDescriptions : IndexedSeq[EntityDescription]) extends ModuleTask