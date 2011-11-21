package ldif.hadoop.types

import org.apache.hadoop.io.{Writable, ArrayWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

class ResultTableArrayWritable extends ArrayWritable(classOf[ResultPatternArrayWritable]) {

  override def toString = {
    val builder = new StringBuilder
    builder.append("ResultTableArrayWritable(")
    var notfirst = false
    for(patternString <- toStrings) {
      builder.append(patternString)
    }
    builder.append(")")
    builder.toString()
  }
}