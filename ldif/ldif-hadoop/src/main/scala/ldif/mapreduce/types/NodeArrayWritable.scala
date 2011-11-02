package ldif.mapreduce.types

import org.apache.hadoop.io.ArrayWritable
import ldif.entity.NodeWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */

class NodeArrayWritable extends ArrayWritable(classOf[NodeWritable]) {
  override def toString = {
    val builder = new StringBuilder
    builder.append("NodeArrayWritable(")
    var notfirst = false
    for(nodeString <- toStrings) {
      if(notfirst)
        builder.append(", ")
      else
        notfirst = true
      builder.append(nodeString)
    }
    builder.append(")")
    builder.toString()
  }
}