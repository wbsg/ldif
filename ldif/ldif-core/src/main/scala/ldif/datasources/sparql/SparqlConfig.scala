package ldif.datasources.sparql

import ldif.module.ModuleConfig

case class SparqlConfig(endpoints : Traversable[EndpointConfig]) extends ModuleConfig
{
}