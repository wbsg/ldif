/* 
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package ldif.local.runtime.impl

import collection.mutable.Queue
import ldif.runtime.Quad
import ldif.runtime.QuadWriter
import ldif.local.runtime.ClonableQuadReader

class QuadQueue extends ClonableQuadReader with QuadWriter {
  private var qq = new Queue[Quad]
  
  // override reader methods
  override def size = qq.size
  override def read = qq.dequeue
  override def hasNext = !qq.isEmpty 

  // override writer methods
  override def write(elem:Quad) = qq.enqueue(elem)
  override def finish() {} //used only for blocking queues

  override def cloneReader: QuadQueue = {
    val queue = new QuadQueue
    queue.qq = this.qq.clone
    queue
  }

}