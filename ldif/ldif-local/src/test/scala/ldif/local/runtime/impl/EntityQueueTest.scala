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

import org.junit.runner.RunWith
import scala.Predef._
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.local.runtime.LocalNode
import ldif.util.Consts
import ldif.entity.{FactumBuilder, Entity, EntityDescription}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 19.05.11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class EntityQueueTest extends FlatSpec with ShouldMatchers {
  it should "read entities in the same order as they were written" in {
    val entityQueue = new EntityQueue(createEntityDescription)
    entityQueue.write(createEntity("_:b1"))
    entityQueue.write(createEntity("_:b2"))
    entityQueue.read.resource.toString should equal ("_:b1")
    entityQueue.read.resource.toString should equal ("_:b2")
  }

  private def createEntity(id: String): Entity = {
     new Entity {
       def resource = LocalNode.createResourceNode(id,Consts.DEFAULT_GRAPH)

       def entityDescription = null

       def factums(patternId: Int, factumBuilder : FactumBuilder) = null
     }
  }

  private def createEntityDescription(): EntityDescription = {
    new EntityDescription(null, null)
  }
}

