package ldif.local.datasources.sparql

import collection.mutable.SynchronizedQueue
import ldif.resource.{Factum, Resource, Path, ResourceFormat}

/**
 * ResourceRetriever which executes multiple SPARQL queries (one for each property path) in parallel and merges the results into single resources.
 */
class ParallelResourceRetriever(endpoint : SparqlEndpoint, pageSize : Int = 1000, graphUri : Option[String] = None) extends ResourceRetriever
{
  private val varPrefix = "v"

  private val maxQueueSize = 1000

  /**
   * Retrieves resources with a given resource format.
   *
   * @param resourceFormat The resource format
   * @param resources The URIs of the resources to be retrieved. If empty, all resources will be retrieved.
   * @return The retrieved resources
   */
  override def retrieve(resourceFormat : ResourceFormat, resources : Seq[String]) : Traversable[Resource] =
  {
    new ResourceTraversable(resourceFormat, resources)
  }

  /**
   * Wraps a Traversable of SPARQL results and retrieves resources from them.
   */
  private class ResourceTraversable(resourceFormat : ResourceFormat, resourceUris : Seq[String]) extends Traversable[Resource]
  {
    override def foreach[U](f : Resource => U) : Unit =
    {
      val pathRetrievers = for(path <- resourceFormat.paths) yield new PathRetriever(resourceUris, resourceFormat, path)

      pathRetrievers.foreach(_.start())

      while(pathRetrievers.forall(_.hasNext))
      {
        val pathValues = for(pathRetriever <- pathRetrievers) yield pathRetriever.next()

        f(new Resource(pathValues.head.uri, pathValues.map(_.values.map(value => new Factum(Seq(value)))).toIndexedSeq, resourceFormat))
      }
    }
  }

  private class PathRetriever(resourceUris : Seq[String], resourceFormat : ResourceFormat, path : Path) extends Thread
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

      queue.dequeue
    }

    override def run()
    {
      try
      {
        if(resourceUris.isEmpty)
        {
          //Query for all resources
          val sparqlResults = queryPath()
          parseResults(sparqlResults)
        }
        else
        {
          //Query for a list of resources
          for(resourceUri <- resourceUris)
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
          sparql += SparqlPathBuilder(path :: Nil, "<" + subjectUri + ">", "?" + varPrefix)
        }
        case None =>
        {
          sparql += resourceFormat.restrictions.toSparql + "\n"
          sparql += SparqlPathBuilder(path :: Nil, "?s", "?" + varPrefix)
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
