/* 
 * LDIF
 *
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ldif.local.datasources.dump

import java.net.URL
import java.util.logging.Logger
import java.io.{BufferedInputStream, FileNotFoundException, InputStream, File}

//import org.apache.http.{HttpEntity, HttpResponse}
//import org.apache.http.client.methods.HttpGet
//import ldif.local.util.HttpClientFactory

/**
 * Streams data from a given file path or URL
 * Accepts N-TRIPLE, N3 and RDF/XML serializations and gzip, bz2 and zip compression.
 **/

@throws(classOf[Exception])
object DumpLoader {
  private val log = Logger.getLogger(getClass.getName)

  //private val httpClient = HttpClientFactory.createHttpClient

  def getStream(sourceLocation : String) = {
    if (sourceLocation == null) {
      throw new Exception("Invalid data location" )
    }

    var url:URL = null
    var file:File = null
    var lang:String = null

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
      lang = ContentTypes.jenaLangFromExtension(file.getName)
    else if (url != null)
      lang = ContentTypes.jenaLangFromExtension(url.getFile)

    if (lang == null) {
      throw new Exception("Unable to determine language for "+ sourceLocation	+ " based on file extension")
    }

    if (file != null)
      getFileStream(file)
    else if (url != null)
      getUrlStream(url)
    else
      throw new Exception("Protocol \"" + url.getProtocol	+ "\" is not supported.")
  }

  def getFileStream(file : File) = {
    log.info("Loading from " + file.getCanonicalPath)
    var inputStream:InputStream = null
    try {
      inputStream = new DecompressingStream(file).getStream
    } catch {
      case e:FileNotFoundException => {
        log.warning(file.getCanonicalPath + " vanished: " + e.getMessage)
        throw e
      }
    }
    new BufferedInputStream(inputStream)
  }

  def getUrlStream(url : URL) = {
    log.info("Loading from " + url.toString)
    var inputStream:InputStream = null

    try  {
      inputStream = new DecompressingStream(url).getStream
    } catch {
      case e:Exception => {
        log.warning(url + " did not provide any data")
        throw e
      }
    }

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
