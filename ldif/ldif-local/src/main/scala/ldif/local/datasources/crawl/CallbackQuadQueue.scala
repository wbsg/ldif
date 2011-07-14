package ldif.local.datasources.crawl

import org.semanticweb.yars.nx.parser.Callback
import org.semanticweb.yars.nx.Node
import java.io.IOException
import ldif.local.runtime.{QuadWriter, LocalNode, Quad}

class CallbackQuadQueue(quadWriter : QuadWriter) extends Callback {

  var statements = 0

  def startDocument{}

  def endDocument{}

  def processStatement(nodes : Array[Node]) {
    if (nodes.size > 2)
      try {
        quadWriter.write(Quad(LocalNode.fromNxNode(nodes(0)), nodes(1).toString, LocalNode.fromNxNode(nodes(2)),null))
      } catch {
        case e:IOException => {
          e.printStackTrace()
          throw new RuntimeException(e)
        }
      }
    statements += 1
  }
}