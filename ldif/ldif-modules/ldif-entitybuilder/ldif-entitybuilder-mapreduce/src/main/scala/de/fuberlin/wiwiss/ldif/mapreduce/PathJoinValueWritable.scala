package de.fuberlin.wiwiss.ldif.mapreduce

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, WritableComparable}
import ldif.entity.NodeWritable

class PathJoinValueWritable (var pathID : IntWritable, var node : NodeWritable) extends WritableComparable[PathJoinValueWritable]{

  def compareTo(other: PathJoinValueWritable) = {
    pathID.compareTo(other.pathID) & node.compareTo(other.node)
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