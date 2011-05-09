package ldif.modules.silk

import ldif.module.ModuleConfig
import de.fuberlin.wiwiss.silk.config.Configuration

/**
 * Silk Configuration.
 */
case class SilkConfig(silkConfig : Configuration) extends ModuleConfig