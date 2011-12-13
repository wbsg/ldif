package ldif.modules.silk.hadoop

import org.apache.hadoop.conf.Configuration
import xml.XML
import java.io.StringReader
import de.fuberlin.wiwiss.silk.config.{LinkingConfig, Prefixes, LinkSpecification}

object Config {

  private val configParam = "silk.config"

  private val LinkSpecParam = "silk.linkSpec"

  def write(job: Configuration, silkConfig : LinkingConfig, linkSpec: LinkSpecification) {
    job.set(configParam, silkConfig.toXML.toString)
    job.set(LinkSpecParam, linkSpec.id)
  }

  def readLinkSpec(job: Configuration) = read(job)._2
  
  def read(job: Configuration): (LinkingConfig, LinkSpecification) = {
    val configStr = job.get(configParam)
    val configXML = XML.load(new StringReader(configStr))
    val config = LinkingConfig.fromXML(configXML)

    val linkSpecId = job.get(LinkSpecParam)
    val linkSpec = config.linkSpec(linkSpecId)

    (config, linkSpec)
  }
}