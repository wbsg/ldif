package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{Quad, QuadReader, QuadWriter}

class QuadQueue extends QuadReader with QuadWriter {
  private var qq = new Queue[Quad]
  
  // override reader methods
  override def size = qq.size
  override def isEmpty = qq.isEmpty
  override def read = qq.dequeue

  // override writer methods
  override def write(elem:Quad) = qq.enqueue(elem)

  override def clone: QuadQueue = {
    val queue = new QuadQueue
    queue.qq = this.qq.clone
    queue
  }
}