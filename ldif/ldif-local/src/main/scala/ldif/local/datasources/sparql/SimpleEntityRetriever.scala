package ldif.local.datasources.sparql

import ldif.entity.{Factum, Entity, Path, EntityDescription}

/**
 * EntityRetriever which executes a single SPARQL query to retrieve the resources.
 */
class SimpleEntityRetriever(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) extends EntityRetriever
{
  private val varPrefix = "v"

  /**
   * Retrieves resources with a given entity description.
   *
   * @param entityDescription The entity description
   * @param resourceUris The URIs of the resources to be retrieved. If empty, all resources will be retrieved.
   * @return The retrieved resources
   */
  override def retrieve(entityDescription : EntityDescription, resourceUris : Seq[String]) : Traversable[Entity] =
  {
    if(resourceUris.isEmpty)
    {
      retrieveAll(entityDescription)
    }
    else
    {
      retrieveList(resourceUris, entityDescription)
    }
  }

  /**
   * Retrieves all resources with a given entity description.
   *
   * @param entityDescription The entity description
   * @return The retrieved resources
   */
  private def retrieveAll(entityDescription : EntityDescription) : Traversable[Entity] =
  {
    //Select
    var sparql = "SELECT DISTINCT "
    sparql += "?s "
    for(i <- 0 until entityDescription.paths.size)
    {
      sparql += "?" + varPrefix + i + " "
    }
    sparql += "\n"

    //Graph
    for(graph <- graphUri) sparql += "FROM <" + graph + ">\n"

    //Body
    sparql += "WHERE {\n"
    if(entityDescription.restrictions.operator.isEmpty && entityDescription.paths.isEmpty)
    {
      sparql += "?s ?" + varPrefix + "_p ?" + varPrefix + "_o "
    }
    else
    {
      sparql += RestrictionSparqlBuilder(entityDescription.restrictions) + "\n"
      sparql += PathSparqlBuilder(entityDescription.paths, "?s", "?" + varPrefix)
    }
    sparql += "}"

    val sparqlResults = endpoint.query(sparql)

    new EntityTraversable(sparqlResults, entityDescription, None)
  }

  /**
   * Retrieves a list of resources.
   *
   * @param resourceUris The URIs of the resources
   * @param entityDescription The entity description
   * @return A sequence of the retrieved resources. If a resource is not in the store, it wont be included in the returned sequence.
   */
  private def retrieveList(resourceUris : Seq[String], entityDescription : EntityDescription) : Seq[Entity] =
  {
    resourceUris.view.flatMap(resourceUri => retrieveEntity(resourceUri, entityDescription))
  }

  /**
   * Retrieves a single resource.
   *
   * @param resourceUri The URI of the resource
   * @param entityDescription The entity description
   * @return Some(resource), if a resource with the given uri is in the Store
   *         None, if no resource with the given uri is in the Store
   */
  def retrieveEntity(resourceUri : String, entityDescription : EntityDescription) : Option[Entity] =
  {
    //Query only one path at once and combine the result into one
    val sparqlResults =
    {
      for((path, pathIndex) <- entityDescription.paths.zipWithIndex;
           results <- retrievePaths(resourceUri, Seq(path))) yield
      {
        results map { case (variable, node) => (varPrefix + pathIndex, node) }
      }
    }

    new EntityTraversable(sparqlResults, entityDescription, Some(resourceUri)).headOption
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
    sparql += PathSparqlBuilder(paths, "<" + resourceUri + ">", "?" + varPrefix)
    sparql += "}"

    endpoint.query(sparql)
  }

  /**
   * Wraps a Traversable of SPARQL results and retrieves resources from them.
   */
  private class EntityTraversable(sparqlResults : Traversable[Map[String, Node]], entityDescription : EntityDescription, subject : Option[String]) extends Traversable[Entity]
  {
    override def foreach[U](f : Entity => U)
    {
      //Remember current subject
      var curSubject : Option[String] = subject

      //Collect values of the current subject
      var values = Array.fill(entityDescription.paths.size)(Seq[Factum]())

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
              f(new Entity(curSubjectUri, values, entityDescription))
            }

            curSubject = resultSubject
            values = Array.fill(entityDescription.paths.size)(Seq[Factum]())
          }
        }

        //Find results for values for the current subject
        if(curSubject.isDefined)
        {
          for((variable, node) <- result if variable.startsWith(varPrefix))
          {
            val id = variable.substring(varPrefix.length).toInt

            values(id) = values(id) :+ new Factum(Seq(node.value))
          }
        }
      }

      for(curSubjectUri <- curSubject)
      {
        f(new Entity(curSubjectUri, values, entityDescription))
      }
    }
  }
}