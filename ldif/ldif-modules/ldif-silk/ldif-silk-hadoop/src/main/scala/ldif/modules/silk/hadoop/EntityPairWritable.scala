package ldif.modules.silk.hadoop

import java.io.{DataOutput, DataInput}
import ldif.entity.EntityWritable

class EntityPairWritable(var source: EntityWritable, var target: EntityWritable) {

  def readFields(in: DataInput) {
    source.readFields(in)
    target.readFields(in)
  }

  def write(out: DataOutput) {
    source.write(out)
    target.write(out)
  }
}