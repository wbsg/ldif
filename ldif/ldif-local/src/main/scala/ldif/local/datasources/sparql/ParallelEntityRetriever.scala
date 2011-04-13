package ldif.local.datasources.sparql

import collection.mutable.SynchronizedQueue
import ldif.entity.{Factum, Entity, Path, EntityDescription}

/**
 * EntityRetriever which executes multiple SPARQL queries (one for each property path) in parallel and merges the results into single entities.
 */
class ParallelEntityRetriever(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) extends EntityRetriever
{
  private val varPrefix = "v"

  private val maxQueueSize = 1000

  /**
   * Retrieves resources with a given entity description.
   *
   * @param entityDescription The entity description
   * @param resources The URIs of the entities to be retrieved. If empty, all entities will be retrieved.
   * @return The retrieved entities
   */
  override def retrieve(entityDescription : EntityDescription, resources : Seq[String]) : Traversable[Entity] =
  {
    new EntityTraversable(entityDescription, resources)
  }

  /**
   * Wraps a Traversable of SPARQL results and retrieves resources from them.
   */
  private class EntityTraversable(entityDescription : EntityDescription, resourceUris : Seq[String]) extends Traversable[Entity]
  {
    override def foreach[U](f : Entity => U) : Unit =
    {
      val pathRetrievers = for(path <- entityDescription.paths) yield new PathRetriever(resourceUris, entityDescription, path)

      pathRetrievers.foreach(_.start())

      while(pathRetrievers.forall(_.hasNext))
      {
        val pathValues = for(pathRetriever <- pathRetrievers) yield pathRetriever.next()

        f(new Entity(pathValues.head.uri, pathValues.map(_.values.map(value => new Factum(Seq(value)))).toIndexedSeq, entityDescription))
      }
    }
  }

  private class PathRetriever(entityUris : Seq[String], entityDescription : EntityDescription, path : Path) extends Thread
  {
    private val queue = new SynchronizedQueue[PathValues]()

    @volatile private var exception : Throwable = null

    def hasNext() : Boolean =
    {
      //If the queue is empty, wait until an element has been read
      while(queue.isEmpty && isAlive)
      {
        Thread.sleep(100)
      }

      //Throw exceptions which occurred during querying
      if(exception != null) throw exception

      !queue.isEmpty
    }

    def next() : PathValues =
    {
      //Throw exceptions which occurred during querying
      if(exception != null) throw exception

      queue.dequeue()
    }

    override def run()
    {
      try
      {
        if(entityUris.isEmpty)
        {
          //Query for all resources
          val sparqlResults = queryPath()
          parseResults(sparqlResults)
        }
        else
        {
          //Query for a list of resources
          for(resourceUri <- entityUris)
          {
            val sparqlResults = queryPath(Some(resourceUri))
            parseResults(sparqlResults, Some(resourceUri))
          }
        }
      }
      catch
      {
        case ex : Throwable => exception = ex
      }
    }

    private def queryPath(fixedSubject : Option[String] = None) =
    {
      //Select
      var sparql = "SELECT DISTINCT "
      if(fixedSubject.isEmpty)
      {
        sparql += "?s "
      }
      sparql += "?" + varPrefix + "0\n"

      //Graph
      for(graph <- graphUri) sparql += "FROM <" + graph + ">\n"

      //Body
      sparql += "WHERE {\n"
      fixedSubject match
      {
        case Some(subjectUri) =>
        {
          sparql += PathSparqlBuilder(path :: Nil, "<" + subjectUri + ">", "?" + varPrefix)
        }
        case None =>
        {
          sparql += RestrictionSparqlBuilder(entityDescription.restrictions) + "\n"
          sparql += PathSparqlBuilder(path :: Nil, "?s", "?" + varPrefix)
        }
      }
      sparql += "}"

      endpoint.query(sparql)
    }

    private def parseResults(sparqlResults : Traversable[Map[String, Node]], fixedSubject : Option[String] = None)
    {
      var currentSubject : Option[String] = fixedSubject
      var currentValues : Set[String] = Set.empty

      for(result <- sparqlResults)
      {
        if(!fixedSubject.isDefined)
        {
          //Check if we are still reading values for the current subject
          val ResourceNode(subject) = result("s")

          if(currentSubject.isEmpty)
          {
            currentSubject = Some(subject)
          }
          else if(subject != currentSubject.get)
          {
            while(queue.size > maxQueueSize)
            {
              Thread.sleep(100)
            }

            queue.enqueue(PathValues(currentSubject.get, currentValues))

            currentSubject = Some(subject)
            currentValues = Set.empty
          }
        }

        for(node <- result.get(varPrefix + "0"))
        {
          currentValues += node.value
        }
      }

      for(s <- currentSubject)
      {
        queue.enqueue(PathValues(s, currentValues))
      }
    }
  }

  private case class PathValues(uri : String, values : Set[String])
}
