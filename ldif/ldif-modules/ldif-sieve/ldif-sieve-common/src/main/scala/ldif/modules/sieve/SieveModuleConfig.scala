package ldif.modules.sieve

import ldif.module.ModuleConfig
import ldif.util.Prefixes

/**
 * Sieve Configuration.
 */
case class SieveModuleConfig(sieveConfig : SieveConfig) extends ModuleConfig