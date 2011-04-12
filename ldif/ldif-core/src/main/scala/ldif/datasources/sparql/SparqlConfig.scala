package ldif.datasources.sparql

import ldif.module.ModuleConfig

class SparqlConfig extends ModuleConfig
{
  val endpoints : Traversable[EndpointConfig] = EndpointConfig("http://www4.wiwiss.fu-berlin.de/drugbank/sparql") :: Nil
}