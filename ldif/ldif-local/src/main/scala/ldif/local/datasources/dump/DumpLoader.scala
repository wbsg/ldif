package ldif.local.datasources.dump

import java.net.URL
import java.util.logging.Logger
import java.io.{BufferedInputStream, FileNotFoundException, InputStream, File}

//import org.apache.http.{HttpEntity, HttpResponse}
//import org.apache.http.client.methods.HttpGet
import ldif.local.util.HttpClientFactory

/**
 * Streams data from a given file path or URL
 * Accepts N-TRIPLE, N3 and RDF/XML serializations and gzip, bz2 and zip compression.
**/

@throws(classOf[Exception])
class DumpLoader(sourceLocation:String) {
  private val log = Logger.getLogger(getClass.getName)

  private val httpClient = HttpClientFactory.createHttpClient

  def getStream = {
    
    if (sourceLocation == null) {
			throw new Exception("Invalid data location" )
		}

    var url:URL = null
    var file:File = null

    try {
      url = new URL(sourceLocation)
    } catch {
        case e:Exception => {
          file = new File(sourceLocation)
          if (!file.exists) {
            throw new Exception("File does not exist: "	+ file.getCanonicalPath)
          }
        }
    }

    if (url != null && url.getProtocol.toLowerCase.equals("file")) {
        file = new File(url.getFile)
    }

    if (file != null)
      getFileStream(file)
    else if (url != null)
      getUrlStream(url)
    else
      throw new Exception("Protocol \"" + url.getProtocol	+ "\" is not supported.")
  }


  private def getFileStream(file:File) = {
    var inputStream:InputStream = null
    val lang = ContentTypes.jenaLangFromExtension(file.getName)

    if (lang == null) {
      throw new Exception("Unable to determine language for "+ sourceLocation	+ " based on file extension")
    }

    log.info("Loading from " + file.getCanonicalPath + " using language " + lang)

    try {
      inputStream = new DecompressingStream(file)
      } catch {
        case e:FileNotFoundException => {
          log.warning(file.getCanonicalPath + " vanished: " + e.getMessage)
          throw e
        }
    }
    new BufferedInputStream(inputStream)
  }


  private def getUrlStream(url:URL) = {
    var inputStream:InputStream = null
    log.info("Loading from " + url)

    //		if (url.getProtocol.toLowerCase.equals("http")) {
    //				val httpget = new HttpGet(sourceLocation)
    //				httpget.getParams.setParameter("Accept",ContentTypes.HTTP_ACCEPT_CONTENT_TYPES)
    //				var response:HttpResponse = null
    //				var entity:HttpEntity = null
    //
    //				try {
    //					response = httpClient.execute(httpget)
    //
    //					entity = response.getEntity
    //					if (entity == null) {
    //						throw new Exception(sourceLocation + " did not provide any data")
    //					}
    //
    //					var lang:String = null
    //					val contentType = entity.getContentType
    //
    //					if (contentType != null) {
    //						lang = ContentTypes.jenaLangFromContentType(contentType.getValue)
    //					}
    //
    //					// use N3 as default format if nothing particular is given
    //					if (contentType.getValue.equals("text/plain")) {
    //						lang = "N3"
    //					}
    //
    //					if (lang == null) {
    //						lang = ContentTypes.jenaLangFromExtension(sourceLocation)
    //						if (lang == null) {
    //							throw new Exception(
    //									"Unable to determine language for "	+ sourceLocation	+
    //                          " (given Content Type: "	+ contentType + ")")
    //						}
    //					}
    //
    //					log.info("Using language " + lang)
    //
    //					inputStream = new DecompressingStream(entity)
    //        }
    //        finally {
    //					if (entity != null) {
    //						httpget.abort
    //            // consumeContent() on a dump is not a good idea
    //					}
    //				}
    //    }
    
    new BufferedInputStream(inputStream)
  }
}