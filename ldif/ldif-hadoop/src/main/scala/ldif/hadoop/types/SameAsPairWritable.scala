package ldif.hadoop.types

import java.io.{DataOutput, DataInput}
import org.apache.hadoop.io.{Text, WritableComparable}
import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/24/11
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */

class SameAsPairWritable(var from: String, var to: String) extends WritableComparable[SameAsPairWritable] {
  def this() {
    this(null, null)
  }

  def write(out: DataOutput) {
    out.writeUTF(from)
    out.writeUTF(to)
  }

  def readFields(in: DataInput) {
    from = in.readUTF()
    to = in.readUTF()
  }

  def compareTo(other: SameAsPairWritable) = {
    if(from==other.from)
      to.compareTo(other.to)
    else
      from.compareTo(other.from)
  }

  override def equals(other: Any): Boolean = {
    if(!other.isInstanceOf[SameAsPairWritable])
      return false
    else
      compareTo(other.asInstanceOf[SameAsPairWritable])==0
  }

  override def hashCode(): Int = {
    from.hashCode() + 31*to.hashCode()
  }

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append("<").append(from).append("> ")
    sb.append("<").append(Consts.SAMEAS_URI).append("> ")
    sb.append("<").append(to).append("> .")
    sb.toString()
  }
}
