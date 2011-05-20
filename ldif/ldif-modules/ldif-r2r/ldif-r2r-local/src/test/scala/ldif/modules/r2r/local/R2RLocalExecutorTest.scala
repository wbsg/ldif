package ldif.modules.r2r.local

import org.scalatest.FlatSpec
import ldif.modules.r2r._
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.fuberlin.wiwiss.r2r._
import ldif.local.runtime.impl.{QuadQueue, EntityQueue, NoEntitiesLeft}
import ldif.entity._
import collection.mutable.HashSet
import CreatorHelperFunctions._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 16.05.11
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class R2RLocalExecutorTest extends FlatSpec with ShouldMatchers {
  val executor = new R2RLocalExecutor
  val repository = new Repository(new FileOrURISource("ldif.modules.r2r/testMapping.ttl"))

  val task = {
    val config = new R2RConfig(repository)
    val module = new R2RModule(config)
    module.tasks.head
  }

  it should "write the expected Quads of the mapping to the Quad Writer" in {
    val executor = new R2RLocalExecutor
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/propertyRenamingDatatypeModifierMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("TestURI1", mapping)
    entity.addFactumRow(Node.createLiteral("testValue", "default"))
    entityQueue.write(entity)
    entityQueue.finish
    val quadQueue = new QuadQueue
    executor.execute(task, Seq(entityQueue), quadQueue)
    (quadQueue.read.toString) should equal ("Quad(<TestURI1>,p2,\"testValue\"^^<bla>,default)")
  }
}



