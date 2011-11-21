package ldif.hadoop.types

import ldif.entity.NodeWritable
import ldif.runtime.Quad
import org.apache.hadoop.io.{Writable, Text}
import java.io.{DataOutput, DataInput}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */

class QuadWritable(var subject: NodeWritable, var property: Text, var obj: NodeWritable, var graph: Text) extends Writable{
  def this() {this(new NodeWritable(), new Text(), new NodeWritable(), new Text())}

  def this(quad: Quad) {this(new NodeWritable(quad.subject), new Text(quad.predicate), new NodeWritable(quad.value), new Text(quad.graph))}

  def write(out: DataOutput) {
    subject.write(out)
    property.write(out)
    obj.write(out)
    graph.write(out)
  }

  def readFields(in: DataInput) {
    subject.readFields(in)
    property.readFields(in)
    obj.readFields(in)
    graph.readFields(in)
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append(subject).append(" <").append(property.toString).append("> ").append(obj).append(" <").append(graph.toString).append("> .").toString()
  }
}