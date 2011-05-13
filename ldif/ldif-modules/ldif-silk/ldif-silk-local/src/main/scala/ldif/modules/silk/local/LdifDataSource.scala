package ldif.modules.silk.local

import de.fuberlin.wiwiss.silk.datasource.DataSource
import de.fuberlin.wiwiss.silk.instance.{Instance, InstanceSpecification}
import ldif.local.runtime.EntityReader

/**
 * Silk DataSource which reads the entities from an EntityReader.
 */
case class LdifDataSource(reader : EntityReader) extends DataSource
{
  override def retrieve(instanceSpec : InstanceSpecification, instances : Seq[String] = Seq.empty) = new Traversable[Instance]
  {
    def foreach[U](f : Instance => U)
    {
      while(!reader.isEmpty)
      {
        val entity = reader.read()

        val values = IndexedSeq.tabulate(instanceSpec.paths.size)(i => entity.factums(i).map(_.head.value).toSet)

        f(new Instance(entity.uri, values, instanceSpec))
      }
    }
  }
}