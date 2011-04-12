package ldif.datasources.sparql

/**
 * Configuration of a SPARQL endpoint.
 *
 * @param uri The URI of the SPARQL endpoint
 * @param login Login required for authentication
 * @param graph Only retrieve instances from a specific graph
 * @param pageSize The number of solutions to be retrieved per SPARQL query (default: 1000)
 * @param pauseTime The number of milliseconds to wait between subsequent query
 * @param retryCount The number of retires if a query fails
 * @param retryPause The number of milliseconds to wait until a failed query is retried
 */
case class EndpointConfig(uri : String, login : Option[EndpointConfig.Login] = None,
                          graph : Option[String] = None, pageSize : Int = 1000,
                          pauseTime : Int = 0, retryCount : Int = 3, retryPause : Int = 1000)

object EndpointConfig
{
  case class Login(user : String, password : String)
}