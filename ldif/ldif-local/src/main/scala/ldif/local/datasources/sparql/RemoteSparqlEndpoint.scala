package ldif.local.datasources.sparql

import xml.{XML, Elem}
import java.util.logging.{Level, Logger}
import io.Source
import java.io.IOException
import java.net._
import ldif.datasources.sparql.EndpointConfig

/**
 * Executes queries on a remote SPARQL endpoint.
 *
 * @param endpoint The configuration of the endpoint
 */
class RemoteSparqlEndpoint(endpoint : EndpointConfig) extends SparqlEndpoint
{
  private val logger = Logger.getLogger(classOf[RemoteSparqlEndpoint].getName)

  private var lastQueryTime = 0L

  override def toString = "SparqlEndpoint(" + endpoint.uri + ")"

  override def query(sparql : String, limit : Int) : Traversable[Map[String, Node]] = new ResultTraversable(sparql, limit)

  private class ResultTraversable(sparql : String, limit : Int) extends Traversable[Map[String, Node]]
  {
    override def foreach[U](f : Map[String, Node] => U)
    {
      var blankNodeCount = 0

      for(offset <- 0 until limit by endpoint.pageSize)
      {
        val xml = executeQuery(sparql + " OFFSET " + offset + " LIMIT " + math.min(endpoint.pageSize, limit - offset))

        val resultsXml = xml \ "results" \ "result"

        for(resultXml <- resultsXml)
        {
          val values = for(binding <- resultXml \ "binding"; node <- binding \ "_") yield node.label match
          {
            case "uri" => (binding \ "@name" text, ResourceNode(node.text))
            case "literal" => (binding \ "@name" text, Literal(node.text))
            case "bnode" =>
            {
              blankNodeCount += 1
              (binding \ "@name" text, BlankNode("bnode" + blankNodeCount))
            }
          }

          f(values.toMap)
        }

        if(resultsXml.size < endpoint.pageSize) return
      }
    }

    /**
     * Executes a SPARQL SELECT query.
     *
     * @param query The SPARQL query to be executed
     * @return Query result in SPARQL Query Results XML Format
     */
    private def executeQuery(query : String) : Elem =
    {
      //Wait until pause time is elapsed since last query
      synchronized
      {
        while(System.currentTimeMillis < lastQueryTime + endpoint.pauseTime) Thread.sleep(endpoint.pauseTime / 10)
        lastQueryTime = System.currentTimeMillis
      }

      //Execute query
      if(logger.isLoggable(Level.FINE)) logger.fine("Executing query on " + endpoint.uri +"\n" + query)

      val url = new URL(endpoint.uri + "?format=application/sparql-results+xml&query=" + URLEncoder.encode(query, "UTF-8"))

      var result : Elem = null
      var retries = 0
      while(result == null)
      {
        val httpConnection = RemoteSparqlEndpoint.openConnection(url, endpoint.login)

        try
        {
          result = XML.load(httpConnection.getInputStream)
        }
        catch
        {
          case ex : IOException =>
          {
            retries += 1
            if(retries > endpoint.retryCount)
            {
              throw ex
            }

            if(logger.isLoggable(Level.INFO))
            {
              val errorStream = httpConnection.getErrorStream
              if(errorStream != null)
              {
                val errorMessage = Source.fromInputStream(errorStream).getLines.mkString("\n")
                logger.info("Query on " + endpoint.uri + " failed. Error Message: '" + errorMessage + "'.\nRetrying in " + endpoint.retryPause + " ms. (" + retries + "/" + endpoint.retryCount + ")")
              }
              else
              {
                logger.info("Query on " + endpoint.uri + " failed:\n" + query + "\nRetrying in " + endpoint.retryPause + " ms. (" + retries + "/" + endpoint.retryCount + ")")
              }
            }

            Thread.sleep(endpoint.retryPause)
          }
          case ex : Exception =>
          {
            logger.log(Level.SEVERE, "Could not execute query on " + endpoint.uri + ":\n" + query, ex)
            throw ex
          }
        }
      }

      //Return result
      if(logger.isLoggable(Level.FINE)) logger.fine("Query Result\n" + result)
      result
    }
  }
}

private object RemoteSparqlEndpoint
{
  /**
   * Opens a new HTTP connection to the endpoint.
   * This method is synchronized to avoid race conditions as the Authentication is set globally in Java.
   */
  private def openConnection(url : URL, login : Option[EndpointConfig.Login]) : HttpURLConnection = synchronized
  {
    //Set authentication
    for(l <- login)
    {
      Authenticator.setDefault(new Authenticator()
      {
        override def getPasswordAuthentication = new PasswordAuthentication(l.user, l.password.toCharArray)
      })
    }

    //Open connection
    val httpConnection = url.openConnection.asInstanceOf[HttpURLConnection]
    httpConnection.setRequestProperty("ACCEPT", "application/sparql-results+xml")

    httpConnection
  }
}
