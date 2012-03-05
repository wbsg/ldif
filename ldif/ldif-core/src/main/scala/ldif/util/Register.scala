package ldif.util

import collection.mutable.ArrayBuffer

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 3/5/12
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */

trait Register[T] {
  private[this] val publishers = new ArrayBuffer[T]

  def addPublisher(reportPublisher: T) {
    this.synchronized {
      publishers.append(reportPublisher)
    }
  }

  def addPublishers(publishers: Seq[T]) {
    this.synchronized {
      for(publisher <- publishers)
        addPublisher(publisher)
    }
  }

  def getPublishers(): IndexedSeq[T] = {
    publishers.readOnly.toIndexedSeq
  }

  def clean() {
    this.synchronized {
      publishers.clear()
    }
  }
}