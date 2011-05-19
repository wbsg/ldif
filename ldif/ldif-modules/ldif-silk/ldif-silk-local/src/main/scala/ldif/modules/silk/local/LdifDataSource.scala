package ldif.modules.silk.local

import de.fuberlin.wiwiss.silk.datasource.DataSource
import de.fuberlin.wiwiss.silk.instance.{Instance, InstanceSpecification}
import ldif.local.runtime.EntityReader
import ldif.modules.silk.LdifInstance

/**
 * Silk DataSource which reads the entities from an EntityReader.
 */
case class LdifDataSource(reader : EntityReader) extends DataSource
{
  override def retrieve(instanceSpec : InstanceSpecification, instances : Seq[String] = Seq.empty) = new Traversable[Instance]
  {
    def foreach[U](f : Instance => U)
    {
      while(reader.hasNext)
      {
        f(new LdifInstance(reader.read(), instanceSpec))
      }
    }
  }
}