package ldif.modules.silk.hadoop

import de.fuberlin.wiwiss.silk.entity.{EntityDescription, Index, Entity}
import xml.XML
import java.io.{DataOutput, StringReader, DataInput}
import ldif.entity.EntityWritable
import de.fuberlin.wiwiss.silk.cache.{BitsetIndex, Partition}
import org.apache.hadoop.io.Writable

class IndexedEntityWritable(var entity: EntityWritable, var index: BitsetIndex) extends Writable {

  def this() = this(null, null)

  override def readFields(in: DataInput) {
    entity.readFields(in)
    index = BitsetIndex.deserialize(in)
  }

  override def write(out: DataOutput) {
    entity.write(out)
    index.serialize(out)
  }

}