package de.fuberlin.wiwiss.ldif.mapreduce.types

import java.io.{DataInput, DataOutput}
import ldif.entity.{NodeWritable, Node, EntityDescription}
import de.fuberlin.wiwiss.ldif.mapreduce.EntityDescriptionMetadata
import org.apache.hadoop.io.{IntWritable, ArrayWritable, WritableComparable}

/**
 * Created by IntelliJ IDEA.
 * User: andrea
 * Date: 8/4/11
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityWritable(var resource : NodeWritable, var resultTable: ArrayWritable, var entityDescriptionID: IntWritable) extends WritableComparable[EntityWritable] {
  def this() {
    this(new NodeWritable(), new ArrayArrayWritable(), new IntWritable())
  }

  def compareTo(other: EntityWritable) = {
    if(resource.compareTo(other.resource)==0)
      entityDescriptionID.compareTo(other.entityDescriptionID)
    else
      resource.compareTo(other.resource)
  }

  def readFields(in: DataInput) {
    resource.readFields(in)
    resultTable.readFields(in)
    entityDescriptionID.readFields(in)
  }

  def write(out: DataOutput) {
    resource.write(out)
    resultTable.write(out)
    entityDescriptionID.write(out)
  }

  def factums(patternId: Int) = {
    convertResultTable(resultTable)(patternId)
  }

  def convertResultTable(results: ArrayWritable): IndexedSeq[Traversable[IndexedSeq[Node]]] = {
    for(pattern <- resultTable.get())
      yield convertPattern(pattern.asInstanceOf[ArrayWritable])
  }

  def convertPattern(pattern: ArrayWritable): Traversable[IndexedSeq[Node]] = {
    for(path <- pattern.get())
      yield convertPath(path.asInstanceOf[ArrayWritable])
  }

  private def convertPath(path: ArrayWritable): IndexedSeq[Node] = {
    for(node <- path.get()) yield node.asInstanceOf[Node]
  }

  override def hashCode = resource.hashCode() + 31 * entityDescriptionID.get()
}

