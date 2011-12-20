package ldif.modules.silk.hadoop.io

import de.fuberlin.wiwiss.silk.cache.Partition
import de.fuberlin.wiwiss.silk.entity.EntityDescription
import xml.XML
import java.io.{StringReader, DataOutput, DataInput}
import org.apache.hadoop.io.Writable

class PartitionWritable(var partition: Partition) extends Writable {

  def this() = this(null)

  def get = partition

  override def readFields(in: DataInput) {
    val entityDescStr = in.readUTF()
    val entityDesc = EntityDescription.fromXML(XML.load(new StringReader(entityDescStr)))
    partition = Partition.deserialize(in, entityDesc)
  }

  override def write(out: DataOutput) {
    val entityDesc = partition.entities.head.desc
    out.writeUTF(entityDesc.toXML.toString)
    partition.serialize(out)
  }
}