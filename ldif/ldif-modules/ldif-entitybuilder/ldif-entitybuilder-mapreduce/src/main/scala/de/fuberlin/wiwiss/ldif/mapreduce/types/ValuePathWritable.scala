package de.fuberlin.wiwiss.ldif.mapreduce.types

import ldif.entity.NodeWritable
import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{IntWritable, ArrayWritable, Writable}
import java.lang.Byte

case class ValuePathWritable (var pathID : IntWritable, var pathType: PathType, var values : NodeArrayWritable) extends Writable {

  def this() {this(new IntWritable(), EntityPathType, new NodeArrayWritable)}

  def write(output : DataOutput) {
    pathID.write(output)
    output.writeByte(pathType.bytePathType)
    values.write(output)
  }

  def readFields(input : DataInput) {
    pathID.readFields(input)
    pathType = PathTypeMap(input.readByte)
    values.readFields(input)
  }

  override def toString = {
    val builder = new StringBuilder
    val pt = pathType match {
      case EntityPathType => "EntityPath"
      case JoinPathType => "JoinPath"
    }
    builder.append(pt).append("(pathID=").append(pathID.toString).append(", ").append(values.toString).append(")")
    builder.toString
  }
}

sealed trait PathType {
  val bytePathType: Int
}

case object EntityPathType extends PathType {
  val bytePathType = 0
  override def toString = "EntityPathType"
}

case object JoinPathType extends PathType {
  val bytePathType = 1
  override def toString = "JoinPathType"
}

case object PathTypeMap {
  val map = Map(0 -> EntityPathType, 1 -> JoinPathType)
  def apply(index: Int) = map(index)
}