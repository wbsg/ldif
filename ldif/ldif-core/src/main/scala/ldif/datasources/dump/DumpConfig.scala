package ldif.datasources.dump

import ldif.module.ModuleConfig

case class DumpConfig (sourceLocations : Traversable[String]) extends ModuleConfig


