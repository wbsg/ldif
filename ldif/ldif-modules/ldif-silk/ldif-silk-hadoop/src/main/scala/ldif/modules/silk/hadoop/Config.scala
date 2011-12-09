package ldif.modules.silk.hadoop

import org.apache.hadoop.conf.Configuration
import xml.XML
import java.io.StringReader
import de.fuberlin.wiwiss.silk.config.{Prefixes, LinkSpecification}

object Config {
  private val LinkSpecParam = "silk.linkSpec"

  def writeLinkSpec(job: Configuration, linkSpec: LinkSpecification) {
    job.set(LinkSpecParam, linkSpec.toXML.toString)
  }
  
  def readLinkSpec(job: Configuration) = {
    val linkSpecStr = job.get(LinkSpecParam)
    val linkSpecXml = XML.load(new StringReader(linkSpecStr))

    LinkSpecification.fromXML(linkSpecXml)(Prefixes.empty)
  }
}