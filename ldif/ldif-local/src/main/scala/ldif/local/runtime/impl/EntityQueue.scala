package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{EntityWriter, EntityReader}
import ldif.entity.{EntityDescription, Entity}

class EntityQueue(val entityDescription : EntityDescription) {
    val qq = new Queue[Entity]
    def reader = new EntityQueueReader(qq, entityDescription)
    def writer = new EntityQueueWriter(qq, entityDescription)
}

class EntityQueueReader(qq:Queue[Entity], val entityDescription : EntityDescription) extends EntityReader{
  override def size = qq.size
  override def isEmpty = qq.isEmpty
  override def read = qq.dequeue
}

class EntityQueueWriter(qq:Queue[Entity], val entityDescription : EntityDescription) extends EntityWriter{
  override def write(elem:Entity) = qq.enqueue(elem)
}