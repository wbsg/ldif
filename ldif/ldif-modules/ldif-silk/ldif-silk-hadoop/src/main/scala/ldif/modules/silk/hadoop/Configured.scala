/*
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import de.fuberlin.wiwiss.silk.util.DPair
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import org.apache.hadoop.mapred.{JobConf, JobConfigurable}
import org.apache.hadoop.conf.Configuration
import de.fuberlin.wiwiss.silk.plugins.Plugins
import xml.XML
import java.io.StringReader
import de.fuberlin.wiwiss.silk.config.{Prefixes, LinkSpecification}

/**
 * Can be extended by all classes who need the current link specification.
 */
trait Configured extends JobConfigurable {

  protected var linkSpec: LinkSpecification = null

  protected var entityDescs: DPair[EntityDescription] = null
  
  protected var isSource: Boolean = false

  protected override def configure(conf: JobConf) {
    linkSpec = readLinkSpec(conf)
    entityDescs = linkSpec.entityDescriptions
    isSource = conf.get(Configured.isSourceParam).toBoolean
  }

  private def readLinkSpec(conf: Configuration) = {
    Plugins.register()

    val linkSpecStr = conf.get(Configured.linkSpecParam)
    val linkSpecXML = XML.load(new StringReader(linkSpecStr))
    val linkSpec = LinkSpecification.fromXML(linkSpecXML)(Prefixes.empty)

    linkSpec
  }
}


object Configured {
  private val linkSpecParam = "silk.linkSpec"
  
  private val isSourceParam = "silk.isSource"

  def write(job: Configuration, linkSpec: LinkSpecification, isSource: Boolean = true) {
    job.set(linkSpecParam, linkSpec.toXML.toString)
    job.set(isSourceParam, isSource.toString)
  }
}