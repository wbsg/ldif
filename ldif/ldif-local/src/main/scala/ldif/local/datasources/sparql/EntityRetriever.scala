package ldif.local.datasources.sparql

import ldif.entity.{Entity, EntityDescription}

/**
 * Retrieves entities from a SPARQL endpoint.
 */
trait EntityRetriever
{
   /**
   * Retrieves instances with a given entity description.
   *
   * @param entityDescription The entity description
   * @param instances The URIs of the instances to be retrieved. If empty, all instances will be retrieved.
   * @return The retrieved instances
   */
  def retrieve(entityDescription : EntityDescription, instances : Seq[String] = Seq.empty) : Traversable[Entity]
}

/**
 * Factory for creating EntityRetriever instances.
 */
object EntityRetriever
{
  //Uses the parallel resource retriever by default as it is generally significantly faster.
  var useParallelRetriever = true

  /**
   * Creates a new EntityRetriever instance.
   */
  def apply(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) : EntityRetriever =
  {
    if(useParallelRetriever)
    {
      new ParallelEntityRetriever(endpoint, pageSize, graphUri)
    }
    else
    {
      new SimpleEntityRetriever(endpoint, pageSize, graphUri)
    }
  }
}
