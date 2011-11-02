package ldif.mapreduce.types

import java.io.{DataInput, DataOutput}
import ldif.entity.NodeWritable
import org.apache.hadoop.io.{IntWritable, WritableComparable}

class PathJoinValueWritable (var pathID : IntWritable, var node : NodeWritable) extends WritableComparable[PathJoinValueWritable]{
  def this() {this(new IntWritable(), new NodeWritable())}

  def compareTo(other: PathJoinValueWritable) = {
    if(pathID.compareTo(other.pathID)==0)
      node.compareTo(other.node)
    else
      pathID.compareTo(other.pathID)
  }

  def readFields(input: DataInput) {
    pathID.readFields(input)
    node.readFields(input)
  }

  def write(output: DataOutput) {
    pathID.write(output)
    node.write(output)
  }
}