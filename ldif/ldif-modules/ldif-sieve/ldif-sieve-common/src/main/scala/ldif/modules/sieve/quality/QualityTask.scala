package ldif.modules.sieve.quality

/*
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import ldif.module.ModuleTask
import ldif.util.Identifier
/**
 * Fusion Task
 * Each task takes care of one <Class> tag in the configuration file.
 * FusionSpec stores what to do for each property of that class.
 */
class QualityTask(val qualityConfig : QualityModuleConfig, val qualitySpec : QualitySpecification) extends ModuleTask
{
  val name : Identifier = qualitySpec.id.toString
  val qualityAssessment : QualityAssessmentProvider = new HashBasedQualityAssessment //TODO FusionTask constructor to pass via QualityConfig?

}