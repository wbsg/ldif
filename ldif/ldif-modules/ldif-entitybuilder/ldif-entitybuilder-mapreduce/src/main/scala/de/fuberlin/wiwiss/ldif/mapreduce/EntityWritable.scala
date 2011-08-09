package de.fuberlin.wiwiss.ldif.mapreduce

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{ArrayWritable, Text, IntWritable, WritableComparable}
import ldif.entity.{Entity, Node, EntityDescription}

/**
 * Created by IntelliJ IDEA.
 * User: andrea
 * Date: 8/4/11
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityWritable(var uri : String, var graph: String, var entityDescription : EntityDescription, var resultTable: ArrayWritable, var entityDescriptionID: IntWritable, edmd: EntityDescriptionMetadata) extends WritableComparable[EntityWritable] with Entity {
  def compareTo(other: EntityWritable) = {
    if(uri.compareTo(other.uri)==0)
      entityDescriptionID.compareTo(other.entityDescriptionID)
    else
      uri.compareTo(other.uri)
  }

  def readFields(in: DataInput) {
    uri = in.readUTF()
    graph = in.readUTF()
    entityDescription = edmd.entityDescriptions(in.readInt())
    resultTable.readFields(in)
    entityDescriptionID.readFields(in)
  }

  def write(out: DataOutput) {
    out.writeUTF(uri)
    out.writeUTF(graph)
    out.writeInt(edmd.entityDescriptionMap(entityDescription))
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

  override def hashCode = uri.hashCode() + 31 * entityDescriptionID.get()
}

