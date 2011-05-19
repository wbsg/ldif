package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{EntityWriter, EntityReader}
import ldif.entity.{EntityDescription, Entity}
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * The EntityQueue is made for exactly one producer and one consumer. Not thread safe!
 */
class EntityQueue(val entityDescription : EntityDescription, capacity: Int) extends EntityReader with EntityWriter {
//    var isWriting = false
  var bufferedEntity: Entity = null
  var buffered = false
  var closed = false

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
    override def size = (if(bufferedEntity!=null) 1 else 0) + entityQueue.size

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
}

/**
 * This object signals the end of entities for an EntityQueue
 */
case object NoEntitiesLeft extends Entity{
  def uri = null

  def entityDescription = null

  def factums(patternId: Int) = null
}