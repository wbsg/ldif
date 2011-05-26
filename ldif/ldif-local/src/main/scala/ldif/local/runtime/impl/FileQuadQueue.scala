package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{Quad, QuadWriter, QuadReader}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 25.05.11
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */

// TODO: Implement
class FileQuadQueue extends QuadReader with QuadWriter {
  private var qq = new Queue[Quad]

  // override reader methods
  override def size = qq.size
  override def isEmpty = qq.isEmpty
  override def read = qq.dequeue

  // override writer methods
  override def write(elem:Quad) = qq.enqueue(elem)

  def restart = {//TODO

  }
}