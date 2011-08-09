package ldif.modules.silk

import ldif.module.ModuleConfig
import de.fuberlin.wiwiss.silk.config.SilkConfig

/**
 * Silk Configuration.
 */
case class SilkModuleConfig(silkConfig : SilkConfig) extends ModuleConfig