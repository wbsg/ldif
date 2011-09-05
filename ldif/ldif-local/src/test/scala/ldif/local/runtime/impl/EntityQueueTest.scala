package ldif.local.runtime.impl

import org.junit.runner.RunWith
import scala.Predef._
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.{Entity, EntityDescription}
import ldif.local.runtime.LocalNode
import ldif.util.Consts

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
    entityQueue.write(createEntity("_:1"))
    entityQueue.write(createEntity("_:2"))
    entityQueue.read.resource.toString should equal ("_:1")
    entityQueue.read.resource.toString should equal ("_:2")
  }

  private def createEntity(id: String): Entity = {
     new Entity {
       def resource = LocalNode.createResourceNode(id,Consts.DEFAULT_GRAPH)

       def entityDescription = null

       def factums(patternId: Int) = null
     }
  }

  private def createEntityDescription(): EntityDescription = {
    new EntityDescription(null, null)
  }
}

