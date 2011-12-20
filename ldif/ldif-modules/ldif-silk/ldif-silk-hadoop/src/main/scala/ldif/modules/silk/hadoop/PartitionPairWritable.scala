package ldif.modules.silk.hadoop

import java.io.{DataOutput, DataInput}
import org.apache.hadoop.io.Writable

class PartitionPairWritable(var source: PartitionWritable, var target: PartitionWritable) extends Writable {

  def this() = this(new PartitionWritable(), new PartitionWritable())

  override def readFields(in: DataInput) {
    source.readFields(in)
    target.readFields(in)
  }

  override def write(out: DataOutput) {
    source.write(out)
    target.write(out)
  }
}