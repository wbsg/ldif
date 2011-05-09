package ldif.modules.silk

import ldif.module.ModuleTask
import ldif.util.Identifier
import de.fuberlin.wiwiss.silk.config.Configuration
import de.fuberlin.wiwiss.silk.linkspec.LinkSpecification

/**
 * Silk Task.
 */
class SilkTask(val silkConfig : Configuration, val linkSpec : LinkSpecification) extends ModuleTask
{
  val name : Identifier = linkSpec.id.toString
}