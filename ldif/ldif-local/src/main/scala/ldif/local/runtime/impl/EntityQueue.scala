package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.local.runtime.{EntityWriter, EntityReader}
import ldif.entity.{EntityDescription, Entity}

class EntityQueue(val entityDescription : EntityDescription) extends EntityReader with EntityWriter {
    var isWriting = false
    val qq = new Queue[Entity]

    // override reader methods
    override def size = qq.size
    override def read = qq.dequeue
    override def hasNext = {
      while (qq.isEmpty && isWriting)
        Thread.sleep(50)
      !qq.isEmpty
    }

    // override writer methods
    override def write(elem:Entity) { qq.enqueue(elem) }
    override def isWriting(bool:Boolean) { isWriting = bool }
}
