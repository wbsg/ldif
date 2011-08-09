package ldif.modules.silk.local

import ldif.local.runtime.{Quad, QuadWriter}
import ldif.entity.Node
import de.fuberlin.wiwiss.silk.output.{Link, LinkWriter}

/**
 * A Silk Link Writer which writes all links to a LDIF Quad Writer.
 */
class LdifLinkWriter(quadWriter : QuadWriter) extends LinkWriter
{
  def write(link : Link, predicateUri : String)
  {
    quadWriter.write(Quad(Node.createUriNode(link.source, null), predicateUri, Node.createUriNode(link.target, null), ""))
  }
}
