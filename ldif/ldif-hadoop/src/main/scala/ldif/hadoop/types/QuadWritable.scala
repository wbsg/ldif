package ldif.hadoop.types

import ldif.entity.NodeWritable
import org.apache.hadoop.io.Text
import ldif.runtime.Quad

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadWritable(var subject: NodeWritable, var property: Text, var obj: NodeWritable, var graph: Text) {
  def this() {this(new NodeWritable(), new Text(), new NodeWritable(), new Text())}

  def this(quad: Quad) {this(new NodeWritable(quad.subject), new Text(quad.predicate), new NodeWritable(quad.value), new Text(quad.graph))}
}