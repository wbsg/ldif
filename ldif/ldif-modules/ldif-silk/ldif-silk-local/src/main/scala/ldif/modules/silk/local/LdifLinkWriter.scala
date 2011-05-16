package ldif.modules.silk.local

import ldif.local.runtime.{Quad, QuadWriter}
import de.fuberlin.wiwiss.silk.output.{Link, LinkWriter}

/**
 * A Silk Link Writer which writes all links to a LDIF Quad Writer.
 */
class LdifLinkWriter(quadWriter : QuadWriter) extends LinkWriter
{
  def write(link : Link, predicateUri : String)
  {
    quadWriter.write(Quad(link.sourceUri, predicateUri, link.targetUri, ""))
  }
}
