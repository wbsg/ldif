package ldif.modules.silk.hadoop

import java.io.{DataOutput, DataInput}
import ldif.entity.EntityWritable
import org.apache.hadoop.io.Writable

class EntityPairWritable(var source: EntityWritable, var target: EntityWritable) extends Writable {
  require(source != null, "source != null")
  require(target != null, "target != null")

  def this() = this(new EntityWritable(), new EntityWritable())

  override def readFields(in: DataInput) {
    source.readFields(in)
    target.readFields(in)
  }

  override def write(out: DataOutput) {
    source.write(out)
    target.write(out)
  }
}