package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{EntityWriter, EntityReader}
import ldif.entity.{EntityDescription, Entity}
import java.util.concurrent.{TimeUnit, BlockingQueue, LinkedBlockingQueue}

/**
 * The EntityQueue is made for exactly one producer and one consumer. Not thread safe!
 */
class EntityQueue(val entityDescription : EntityDescription, capacity: Int) extends EntityReader with EntityWriter {
//    var isWriting = false
  var bufferedEntity: Entity = null
  var buffered = false
  var stillArriving = true // Gives the EntityQueue a hint if entities are still being put into it
  var closed = false
  var finished = false

//    val qq = new Queue[Entity]
  val entityQueue = {
    if(capacity==0)
      new LinkedBlockingQueue[Entity]()
    else
      new LinkedBlockingQueue[Entity](capacity)
  }

  def this(entityDescription: EntityDescription) {
    this(entityDescription, 0)
  }

    // override reader methods
    override def size = {
      if(closed)
        0
      var queueSize = entityQueue.size
      if(queueSize > 0 && stillArriving==false)
        queueSize = queueSize - 1
      queueSize = queueSize + (if(bufferedEntity!=null) 1 else 0)

      queueSize
    }

  /**
   * returns the next entity. Should be used together with hasNext to avoid nulls and/or NoEntitiesLeft object.
   */
    override def read = {
      if(buffered) {
        buffered = false
        bufferedEntity
      } else {
        entityQueue.poll
      }
    }

  /**
   * Like read, but only waits the specified time. If no entity is in the queue until then, null is returned
   */
  def timedRead(timeout: Long, timeUnit: TimeUnit) = {
    if(buffered) {
      buffered = false
      bufferedEntity
    } else {
      entityQueue.poll(timeout, timeUnit)
    }
  }

  /**
   * returns true if there are still entities in the queue or false if the end is reached. It blocks if no
   * entities are currently in the queue, because it can't be decided if there will still be another entity.
   */
    override def hasNext = {
      if(closed)
        false
      else if (buffered)
        true
      else if (entityQueue.size>0) {
        !(entityQueue.peek eq NoEntitiesLeft)
      }
      else {
        entityQueue.take match {
          case NoEntitiesLeft => closed=true; false
          case entity => bufferedEntity = entity; buffered=true; true
        }
      }
    }

  /**
   *   The write method blocks if queue is full
    */
    override def write(elem:Entity) { entityQueue.put(elem) }

  /**
   * Signals the EntityQueue that no entities will follow.
   */
  def finish() {
    stillArriving = false
    write(NoEntitiesLeft)
  }
}

/**
 * This object signals the end of entities for an EntityQueue
 */
case object NoEntitiesLeft extends Entity{
  def uri = null

  def entityDescription = null

  def factums(patternId: Int) = null
}