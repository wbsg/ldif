package ldif.datasources.dump

import ldif.module.ModuleConfig

case class DumpConfig (dataLocationUrls : Traversable[String]) extends ModuleConfig


