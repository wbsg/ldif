/*
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.modules.silk.hadoop

import org.apache.hadoop.conf.Configuration
import xml.XML
import java.io.StringReader
import de.fuberlin.wiwiss.silk.config.{LinkingConfig, Prefixes, LinkSpecification}
import de.fuberlin.wiwiss.silk.plugins.Plugins

object Config {

  private val configParam = "silk.config"

  private val LinkSpecParam = "silk.linkSpec"

  def write(job: Configuration, silkConfig : LinkingConfig, linkSpec: LinkSpecification) {
    job.set(configParam, silkConfig.toXML.toString)
    job.set(LinkSpecParam, linkSpec.id)
  }

  def readLinkSpec(job: Configuration) = read(job)._2
  
  def read(job: Configuration): (LinkingConfig, LinkSpecification) = {
    Plugins.register()

    val configStr = job.get(configParam)
    val configXML = XML.load(new StringReader(configStr))
    val config = LinkingConfig.fromXML(configXML)

    val linkSpecId = job.get(LinkSpecParam)
    val linkSpec = config.linkSpec(linkSpecId)

    (config, linkSpec)
  }
}