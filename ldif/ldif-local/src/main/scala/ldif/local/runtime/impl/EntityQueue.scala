package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{EntityWriter, EntityReader}
import ldif.entity.{EntityDescription, Entity}

class EntityQueue(val entityDescription : EntityDescription) {
    var hasNext = false
    val qq = new Queue[Entity]    
    def reader = new EntityQueueReader(this)
    def writer = new EntityQueueWriter(this)
}

class EntityQueueReader(eq:EntityQueue) extends EntityReader{
  val entityDescription = eq.entityDescription
  override def size = eq.qq.size
  override def read = eq.qq.dequeue       
  override def hasNext = {
    while (eq.qq.isEmpty && eq.hasNext)
      Thread.sleep(50)
    !eq.qq.isEmpty
  }
}

class EntityQueueWriter(eq:EntityQueue) extends EntityWriter{
  val entityDescription = eq.entityDescription
  override def write(elem:Entity) { eq.qq.enqueue(elem) }
  override def hasNext(bool:Boolean) { eq.hasNext = bool }
}
