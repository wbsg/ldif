/*
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def addPublisher(publisher: T) {
    this.synchronized {
      publishers.append(publisher)
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

  def getPublisher(index: Int): Option[T] = {
    this.synchronized {
      if(index>=0 && index<publishers.length)
        return Some(publishers(index))
      else
        return None
    }
  }
  
  def getIndex(publisher: T): Option[Int] = {
    this.synchronized {
      for ((p,index) <- publishers.zipWithIndex)
        if (p == publisher)
          return Some(index)
    }
    None
  }

  def clean() {
    this.synchronized {
      publishers.clear()
    }
  }
}