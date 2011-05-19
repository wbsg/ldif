package ldif.local.runtime.impl

import org.junit.runner.RunWith
import scala.Predef._
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.{Entity, EntityDescription}

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
    entityQueue.write(createEntity("1"))
    entityQueue.write(createEntity("2"))
    entityQueue.read.uri should equal ("1")
    entityQueue.read.uri should equal ("2")
  }

  private def createEntity(id: String): Entity = {
     new Entity {
       def uri = id

       def entityDescription = null

       def factums(patternId: Int) = null
     }
  }

  private def createEntityDescription(): EntityDescription = {
    new EntityDescription(null, null)
  }
}

