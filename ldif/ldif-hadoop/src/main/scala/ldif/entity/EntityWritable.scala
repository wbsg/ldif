package ldif.entity

import java.io.{DataInput, DataOutput}
import org.apache.hadoop.io.{Writable, IntWritable, ArrayWritable, WritableComparable}
import ldif.hadoop.types.{ResultPatternArrayWritable, NodeArrayWritable, ResultTableArrayWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andrea
 * Date: 8/4/11
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */

class EntityWritable(var resource : NodeWritable, var resultTable: ResultTableArrayWritable, var entityDescriptionID: IntWritable) extends WritableComparable[EntityWritable] with Entity {
  def this() {
    this(new NodeWritable(), new ResultTableArrayWritable(), new IntWritable())
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
    EntityWritable.convertResultTable(resultTable)(patternId)
  }

  override def hashCode = resource.hashCode() + 31 * entityDescriptionID.get()

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append("EntityWritable(").append(resource).append(", ").append(resource.graph).append(")\n")
    sb.append("   Results:\n   ")
    sb.append(resultTable)
    sb.toString
  }
}

object EntityWritable {
  def convertResultTable(resultTable: IndexedSeq[Traversable[IndexedSeq[NodeWritable]]]): ResultTableArrayWritable = {
    val result = new ResultTableArrayWritable()
    result.set((for(patternResult <- resultTable)
      yield convertPattern(patternResult)).toArray)
    result
  }

  private def convertPattern(patternResult: Traversable[IndexedSeq[NodeWritable]]): ArrayWritable = {
    val result = new ResultPatternArrayWritable()
    result.set((for(row <- patternResult)
      yield convertPath(row)).toArray)
    result
  }

  private def convertPath(path: IndexedSeq[NodeWritable]): ArrayWritable = {
    val result = new NodeArrayWritable()
    result.set(path.toArray)
    result
  }

  def convertResultTable(results: ArrayWritable): IndexedSeq[Traversable[IndexedSeq[NodeTrait]]] = {
    for(pattern <- results.get())
      yield convertPattern(pattern.asInstanceOf[ArrayWritable])
  }

  private def convertPattern(pattern: ArrayWritable): Traversable[IndexedSeq[NodeTrait]] = {
    for(path <- pattern.get())
      yield convertPath(path.asInstanceOf[ArrayWritable])
  }

  private def convertPath(path: ArrayWritable): IndexedSeq[NodeTrait] = {
    for(node <- path.get()) yield node.asInstanceOf[NodeTrait]
  }
}

