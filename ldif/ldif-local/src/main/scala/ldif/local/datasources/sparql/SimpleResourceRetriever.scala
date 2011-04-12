package ldif.local.datasources.sparql

import ldif.resource.{Factum, Resource, Path, ResourceFormat}

/**
 * ResourceRetriever which executes a single SPARQL query to retrieve the resources.
 */
class SimpleResourceRetriever(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) extends ResourceRetriever
{
  private val varPrefix = "v"

  /**
   * Retrieves resources with a given resource format.
   *
   * @param resourceFormat The resource format
   * @param resourceUris The URIs of the resources to be retrieved. If empty, all resources will be retrieved.
   * @return The retrieved resources
   */
  override def retrieve(resourceFormat : ResourceFormat, resourceUris : Seq[String]) : Traversable[Resource] =
  {
    if(resourceUris.isEmpty)
    {
      retrieveAll(resourceFormat)
    }
    else
    {
      retrieveList(resourceUris, resourceFormat)
    }
  }

  /**
   * Retrieves all resources with a given resource format.
   *
   * @param resourceFormat The resource format
   * @return The retrieved resources
   */
  private def retrieveAll(resourceFormat : ResourceFormat) : Traversable[Resource] =
  {
    //Select
    var sparql = "SELECT DISTINCT "
    sparql += "?s "
    for(i <- 0 until resourceFormat.paths.size)
    {
      sparql += "?" + varPrefix + i + " "
    }
    sparql += "\n"

    //Graph
    for(graph <- graphUri) sparql += "FROM <" + graph + ">\n"

    //Body
    sparql += "WHERE {\n"
    if(resourceFormat.restrictions.toSparql.isEmpty && resourceFormat.paths.isEmpty)
    {
      sparql += "?s ?" + varPrefix + "_p ?" + varPrefix + "_o "
    }
    else
    {
      sparql += resourceFormat.restrictions.toSparql + "\n"
      sparql += SparqlPathBuilder(resourceFormat.paths, "?s", "?" + varPrefix)
    }
    sparql += "}"

    val sparqlResults = endpoint.query(sparql)

    new ResourceTraversable(sparqlResults, resourceFormat, None)
  }

  /**
   * Retrieves a list of resources.
   *
   * @param resourceUris The URIs of the resources
   * @param resourceFormat The resource format
   * @return A sequence of the retrieved resources. If a resource is not in the store, it wont be included in the returned sequence.
   */
  private def retrieveList(resourceUris : Seq[String], resourceFormat : ResourceFormat) : Seq[Resource] =
  {
    resourceUris.view.flatMap(resourceUri => retrieveResource(resourceUri, resourceFormat))
  }

  /**
   * Retrieves a single resource.
   *
   * @param resourceUri The URI of the resource
   * @param resourceFormat The resource format
   * @return Some(resource), if a resource with the given uri is in the Store
   *         None, if no resource with the given uri is in the Store
   */
  def retrieveResource(resourceUri : String, resourceFormat : ResourceFormat) : Option[Resource] =
  {
    //Query only one path at once and combine the result into one
    val sparqlResults =
    {
      for((path, pathIndex) <- resourceFormat.paths.zipWithIndex;
           results <- retrievePaths(resourceUri, Seq(path))) yield
      {
        results map { case (variable, node) => (varPrefix + pathIndex, node) }
      }
    }

    new ResourceTraversable(sparqlResults, resourceFormat, Some(resourceUri)).headOption
  }

  private def retrievePaths(resourceUri : String, paths : Seq[Path]) =
  {
    //Select
    var sparql = "SELECT DISTINCT "
    for(i <- 0 until paths.size)
    {
      sparql += "?" + varPrefix + i + " "
    }
    sparql += "\n"

    //Graph
    for(graph <- graphUri) sparql += "FROM <" + graph + ">\n"

    //Body
    sparql += "WHERE {\n"
    sparql += SparqlPathBuilder(paths, "<" + resourceUri + ">", "?" + varPrefix)
    sparql += "}"

    endpoint.query(sparql)
  }

  /**
   * Wraps a Traversable of SPARQL results and retrieves resources from them.
   */
  private class ResourceTraversable(sparqlResults : Traversable[Map[String, Node]], resourceFormat : ResourceFormat, subject : Option[String]) extends Traversable[Resource]
  {
    override def foreach[U](f : Resource => U) : Unit =
    {
      //Remember current subject
      var curSubject : Option[String] = subject

      //Collect values of the current subject
      var values = Array.fill(resourceFormat.paths.size)(Traversable[Factum]())

      for(result <- sparqlResults)
      {
        //If the subject is unknown, find binding for subject variable
        if(subject.isEmpty)
        {
          //Check if we are still reading values for the current subject
          val resultSubject = result.get("s") match
          {
            case Some(ResourceNode(value)) => Some(value)
            case _ => None
          }

          if(resultSubject != curSubject)
          {
            for(curSubjectUri <- curSubject)
            {
              f(new Resource(curSubjectUri, values, resourceFormat))
            }

            curSubject = resultSubject
            values = Array.fill(resourceFormat.paths.size)(Traversable[Factum]())
          }
        }

        //Find results for values for the current subject
        if(curSubject.isDefined)
        {
          for((variable, node) <- result if variable.startsWith(varPrefix))
          {
            val id = variable.substring(varPrefix.length).toInt

            values(id) += new Factum(Seq(node.value))
          }
        }
      }

      for(curSubjectUri <- curSubject)
      {
        f(new Resource(curSubjectUri, values, resourceFormat))
      }
    }
  }
}