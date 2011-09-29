package ldif.local.scheduler

import org.semanticweb.yars.util.CallbackNxOutputStream
import java.io.{IOException, OutputStream}
import org.semanticweb.yars.nx.Node
import collection.mutable.{HashSet, Set}

class CallbackOutputStream(val out : OutputStream) extends CallbackNxOutputStream(out) {

  var statements = 0
  var graphs : Set[String] = new HashSet[String]
  val space = " ".getBytes
	val dot_nl = ("."+System.getProperty("line.separator")).getBytes

  override def processStatement(nodes : Array[Node]) {
    try {
      for(n <- nodes){
        out.write(n.toN3.getBytes)
        out.write(space)
      }
      //graphs += nodes(3).toString
      out.write(dot_nl)
    } catch {
      case e:IOException => {
        e.printStackTrace()
        throw new RuntimeException(e)
      }
    }
    //super.processStatement(nodes)
    graphs += nodes(3).toString
    statements += 1
  }
}