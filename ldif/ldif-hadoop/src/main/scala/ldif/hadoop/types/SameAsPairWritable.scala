package ldif.hadoop.types

import java.io.{DataOutput, DataInput}
import org.apache.hadoop.io.{Text, WritableComparable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairWritable(var from: String, var to: String, var toBeExtended: Boolean) extends WritableComparable[SameAsPairWritable] {
  def this() {
    this(null, null, true)
  }

  def write(out: DataOutput) {
    out.writeUTF(from)
    out.writeUTF(to)
    out.writeBoolean(toBeExtended)
  }

  def readFields(in: DataInput) {
    from = in.readUTF()
    to = in.readUTF()
    toBeExtended = in readBoolean()
  }

  def compareTo(other: SameAsPairWritable) = {
    if(from==other.from)
      to.compareTo(other.to)
    else
      from.compareTo(other.from)
  }
}
