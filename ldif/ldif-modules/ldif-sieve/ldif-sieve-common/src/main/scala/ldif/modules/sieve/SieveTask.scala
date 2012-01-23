package ldif.modules.sieve

import fusion.{PassItOn, FusionFunction}
import ldif.module.ModuleTask
import ldif.util.Identifier

/**
 * Sieve Task.
 */
class SieveTask(val sieveConfig : SieveModuleConfig, val sieveSpec : FusionSpecification) extends ModuleTask
{
  val name : Identifier = sieveSpec.id.toString
  val qualityAssessment : QualityAssessment = new QualityAssessment //TODO SieveTask constructor to pass QualitySpec

}