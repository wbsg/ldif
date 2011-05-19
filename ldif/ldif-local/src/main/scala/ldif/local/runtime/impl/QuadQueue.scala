package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{Quad, QuadReader, QuadWriter}

class QuadQueue extends QuadReader with QuadWriter {
  val qq = new Queue[Quad]
  
  // override reader methods
  override def size = qq.size
  override def isEmpty = qq.isEmpty
  override def read = qq.dequeue

  // override writer methods
  override def write(elem:Quad) = qq.enqueue(elem)
}