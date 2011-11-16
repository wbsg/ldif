package ldif.hadoop.types

import org.apache.hadoop.io.ArrayWritable

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/17/11
 * Time: 11:07 AM
 * To change this template use File | Settings | File Templates.
 */

class ArrayArrayWritable extends ArrayWritable(classOf[ArrayWritable]) {
  override def toString = {
    val builder = new StringBuilder
    builder.append("ArrayArrayWritable(")
    var notfirst = false
    for(arrayString <- toStrings) {
      if(notfirst)
        builder.append(", ")
      else
        notfirst = true
      builder.append(arrayString)
    }
    builder.append(")")
    builder.toString()
  }
}