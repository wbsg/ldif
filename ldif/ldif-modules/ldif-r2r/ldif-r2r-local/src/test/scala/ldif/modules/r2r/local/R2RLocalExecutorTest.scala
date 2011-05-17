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

  val task = {
    val config = new R2RConfig(repository)
    val module = new R2RModule(config)
    module.tasks.head
  }

  it should "write the expected Quads to the Quad Writer" in {
    val executor = new R2RLocalExecutor
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/testMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("TestURI1", mapping)
    entity.addFactumRow(Node.createLiteral("testValue", "default"))
    entityQueue.writer.write(entity)
    val quadQueue = new QuadQueue
    executor.execute(task, Seq(entityQueue.reader), quadQueue.writer)
    (quadQueue.reader.read.toString) should equal ("Quad(<TestURI1>,<p2>,\"testValue\"^^<bla>,default)")
  }

  private def createEntityQueue(entityDescription: EntityDescription): EntityQueue = {
    new EntityQueue(entityDescription)
  }

  private def addEntityToEntityQueue(entity: Entity, entityQueue: EntityQueue) {
    entityQueue.writer.write(entity)
  }

  private def createEntity(entityUri: String, mapping: LDIFMapping): MutableEntity = {
    new MutableEntity(entityUri, mapping)
  }

  private def getMapping(mappingURI: String, repository: Repository): LDIFMapping = {
    LDIFMapping(repository.getMappingOfUri(mappingURI))
  }
}

class MutableEntity(entityUri: String, mapping: LDIFMapping) extends Entity {
  val uri = entityUri
  val resultTable = new HashSet[FactumRow] with FactumTable
  override def factums(patternID: Int) : FactumTable = resultTable
  def entityDescription = mapping.entityDescription
  def addFactumRow(nodes: Node*) {
    val row = new FactumRow {
      def length = nodes.length
      def apply(idx: Int) = nodes(idx)
    }
    resultTable.add(row)
  }
}