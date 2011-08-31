package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{Quad, ClonableQuadReader, QuadWriter}

class QuadQueue extends ClonableQuadReader with QuadWriter {
  private var qq = new Queue[Quad]
  
  // override reader methods
  override def size = qq.size
  override def read = qq.dequeue
  override def hasNext = !qq.isEmpty 

  // override writer methods
  override def write(elem:Quad) = qq.enqueue(elem)
  override def finish() {} //used only for blocking queues

  override def cloneReader: QuadQueue = {
    val queue = new QuadQueue
    queue.qq = this.qq.clone
    queue
  }

}