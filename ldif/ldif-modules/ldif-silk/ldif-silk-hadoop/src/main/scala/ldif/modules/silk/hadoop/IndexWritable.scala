package ldif.modules.silk.hadoop

import java.io.{DataOutput, DataInput}
import org.apache.hadoop.io.{WritableComparable, Writable}

class IndexWritable(var indices: Set[Int]) extends WritableComparable[IndexWritable] {

  def this() = this(null)

  //TODO improve
  override def compareTo(other: IndexWritable) = {
    if(this.indices == other.indices)
      0
    else
      1//indices.size.compareTo(other.indices.size)
  }

  override def readFields(in: DataInput) {
    indices = Array.fill(in.readInt())(in.readInt()).toSet
  }

  override def write(out: DataOutput) {
    out.writeInt(indices.size)
    for(i <- indices)
      out.writeInt(i)
  }
}