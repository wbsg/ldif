package ldif.datasources.sparql

import ldif.module.ModuleTask
import ldif.util.Identifier

class SparqlTask(override val name : Identifier, val endpoint : EndpointConfig) extends ModuleTask