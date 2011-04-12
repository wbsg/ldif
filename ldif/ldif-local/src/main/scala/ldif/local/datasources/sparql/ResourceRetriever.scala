package ldif.local.datasources.sparql

import ldif.resource.{Resource, ResourceFormat}

/**
 * Retrieves resources from a SPARQL endpoint.
 */
trait ResourceRetriever
{
   /**
   * Retrieves instances with a given resource format.
   *
   * @param resourceFormat The resource format
   * @param instances The URIs of the instances to be retrieved. If empty, all instances will be retrieved.
   * @return The retrieved instances
   */
  def retrieve(resourceFormat : ResourceFormat, instances : Seq[String]) : Traversable[Resource]
}

/**
 * Factory for creating ResourceRetriever instances.
 */
object ResourceRetriever
{
  //Uses the parallel resource retriever by default as it is generally significantly faster.
  var useParallelRetriever = true

  /**
   * Creates a new ResourceRetriever instance.
   */
  def apply(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) : ResourceRetriever =
  {
    if(useParallelRetriever)
    {
      new ParallelResourceRetriever(endpoint, pageSize, graphUri)
    }
    else
    {
      new SimpleResourceRetriever(endpoint, pageSize, graphUri)
    }
  }
}
