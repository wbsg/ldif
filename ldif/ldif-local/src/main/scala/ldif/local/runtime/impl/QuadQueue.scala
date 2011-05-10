package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{Quad, QuadReader, QuadWriter}

class QuadQueue {
    val qq = new Queue[Quad]
    def reader = new QuadQueueReader(qq)
    def writer = new QuadQueueWriter(qq)
}

class QuadQueueReader(qq:Queue[Quad]) extends QuadReader{
  override def size = qq.size
  override def isEmpty = qq.isEmpty
  override def read = qq.dequeue
}

class QuadQueueWriter(qq:Queue[Quad]) extends QuadWriter{
  override def write(elem:Quad) = qq.enqueue(elem)
}