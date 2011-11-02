package ldif.mapreduce.types

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, WritableComparable}
import ldif.entity.NodeWritable

class EntityDescriptionNodeWritable(var entityDescriptionID : IntWritable, var node : NodeWritable) extends WritableComparable[EntityDescriptionNodeWritable]{

  def this() {
    this(new IntWritable(), new NodeWritable())
  }

  override def compareTo(other: EntityDescriptionNodeWritable): Int = {
    if(entityDescriptionID.compareTo(other.entityDescriptionID)!=0)
      return entityDescriptionID.compareTo(other.entityDescriptionID)
    else
      return node.compareTo(other.node)
  }

  def readFields(input: DataInput) {
    entityDescriptionID.readFields(input)
    node.readFields(input)
  }

  def write(output: DataOutput) {
    entityDescriptionID.write(output)
    node.write(output)
  }

  def set(entityDescriptionID: IntWritable,  node: NodeWritable) {
    this.entityDescriptionID = entityDescriptionID
    this.node = node
  }
}