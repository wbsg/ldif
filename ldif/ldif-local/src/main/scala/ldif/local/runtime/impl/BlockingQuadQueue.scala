package ldif.local.runtime.impl

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import ldif.local.runtime.Quad

/**
 * BlockingQuadQueue is made for exactly one producer and one consumer. Not thread safe!
 */

class BlockingQuadQueue(capacity: Int) extends QuadQueue {

  var bufferedQuad: Quad = null
  var buffered = false
  var stillArriving = true // Gives the QuadQueue a hint if quads are still being put into it
  var closed = false
  var totalSize = 0

  val quadQueue = {
    if(capacity==0)
      new LinkedBlockingQueue[Quad]()
    else
      new LinkedBlockingQueue[Quad](capacity)
  }

  def this() {
    this(0)
  }

  // override reader methods
  override def size = {
    if(closed)
      0
    var queueSize = quadQueue.size
    if(queueSize > 0 && stillArriving==false)
      queueSize = queueSize - 1
    queueSize = queueSize + (if(bufferedQuad!=null) 1 else 0)

    queueSize
  }

  /**
   * Returns the next quad.
   * Should be used together with hasNext to avoid nulls and/or NoQuadsLeft object.
   */
  override def read = {
    if(buffered) {
      buffered = false
      bufferedQuad
    } else {
      quadQueue.poll
    }
  }

  /**
   * Like read, but only waits the specified time.
   * If no quad is in the queue until then, null is returned.
   */
  def timedRead(timeout: Long, timeUnit: TimeUnit) = {
    if(buffered) {
      buffered = false
      bufferedQuad
    } else {
      quadQueue.poll(timeout, timeUnit)
    }
  }

  /**
   * Returns true if there are still quads in the queue, false if the end is reached.
   * It blocks if no quads are currently in the queue,
   * since it can't be decided if there will still be another quad.
   */
  override def hasNext = {
    if(closed)
      false
    else if (buffered)
      true
    else if (quadQueue.size>0) {
      !(quadQueue.peek eq NoQuadsLeft)
    }
    else {
      quadQueue.take match {
        case NoQuadsLeft => closed=true; false
        case quad => bufferedQuad = quad; buffered=true; true
      }
    }
  }

  /**
   * The write method blocks if queue is full
   */
  override def write(elem:Quad) {
    quadQueue.put(elem)
    totalSize = totalSize +1
  }

  /**
   * Signals the QuadQueue that no quads will follow
   */
  override def finish() {
    stillArriving = false
    write(NoQuadsLeft)
    totalSize = totalSize -1
  }

  /**
   * This object signals the end of quads for an QuadQueue
   */
  case object NoQuadsLeft extends Quad(null,null,null,null)

}