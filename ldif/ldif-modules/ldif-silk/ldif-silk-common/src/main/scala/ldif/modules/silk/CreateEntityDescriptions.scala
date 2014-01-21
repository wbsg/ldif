/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk

import ldif.entity.EntityDescription
import de.fuberlin.wiwiss.silk.config.{LinkSpecification, Prefixes}

/**
 * Generates EntityDescriptions from a LinkSpecification.
 */
object CreateEntityDescriptions {
  /**
   * Generates EntityDescriptions from a LinkSpecification.
   */
  def apply(linkSpec : LinkSpecification)(implicit prefixes : Prefixes) : Seq[EntityDescription] = {
    linkSpec.entityDescriptions.toSeq.map(ConvertEntityDescription.apply)
  }
}