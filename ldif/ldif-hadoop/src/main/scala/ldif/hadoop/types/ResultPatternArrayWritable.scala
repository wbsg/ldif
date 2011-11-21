package ldif.hadoop.types

import org.apache.hadoop.io.ArrayWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/21/11
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */

class ResultPatternArrayWritable extends ArrayWritable(classOf[NodeArrayWritable]) {
  override def toString = {
    val builder = new StringBuilder
    builder.append("  ResultPatternArrayWritable(")
    for(rowString <- toStrings) {
      builder.append("    ").append(rowString).append("\n")
    }
    builder.append("  )\n")
    builder.toString()
  }
}