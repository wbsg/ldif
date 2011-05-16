package ldif.modules.r2r.local

import org.scalatest.FlatSpec
import ldif.modules.r2r._
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import de.fuberlin.wiwiss.r2r._
import ldif.local.runtime.impl.{QuadQueue, EntityQueue}
import ldif.entity._
import collection.mutable.HashSet

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
  val mapping = LDIFMapping(repository.getMappingOfUri("http://mappings.dbpedia.org/r2r/testMapping"))
  val entityQueue = new EntityQueue(mapping.entityDescription)
  val quadQueue = new QuadQueue

  entityQueue.writer.write(new Entity{
    val uri = "TestURI1"
    override def factums(patternID: Int) : FactumTable = {
      val table = new HashSet[FactumRow] with FactumTable
      val row = new FactumRow {
        def length = 1

        def apply(idx: Int) = Node.createLiteral("testValue", "default")
      }
//      row += Node.createLiteral("testValue", "default")
      table.add(row)
      table
    }

    def entityDescription = mapping.entityDescription
  })

  val task = {
    val config = new R2RConfig(repository)
    val module = new R2RModule(config)
    module.tasks.head
  }

  it should "write the expected Quads to the Quad Writer" in {
    executor.execute(task, Seq(entityQueue.reader), quadQueue.writer)
    (quadQueue.reader.read.toString) should equal ("Quad(<TestURI1>,<p2>,\"testValue\"^^<bla>,default)")
  }
}