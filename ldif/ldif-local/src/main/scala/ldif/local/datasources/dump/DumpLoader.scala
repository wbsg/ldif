/* 
 * LDIF
 *
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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
import org.slf4j.LoggerFactory
import org.deri.any23.Any23
import org.deri.any23.source.ByteArrayDocumentSource
import java.io._
import org.deri.any23.writer.NTriplesWriter
import ldif.local.runtime.QuadReader
import ldif.local.runtime.impl.FileQuadReader


/**
 * Streams data from a given file path or URL
 * Accepts N-TRIPLE, N3 and RDF/XML serializations and gzip, bz2 and zip compression.
 **/

@throws(classOf[Exception])
object DumpLoader {
  private val log = LoggerFactory.getLogger(getClass.getName)

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
      lang = ContentTypes.getLangFromExtension(file.getName)
    else if (url != null)
      lang = ContentTypes.getLangFromExtension(url.getFile)

    if (lang == null) {
      throw new Exception("Unable to determine language for "+ sourceLocation	+ " based on file extension")
    }

    if (file != null)
      getFileStream(file, lang)
    else if (url != null)
      getUrlStream(url, lang)
    else
      throw new Exception("Protocol \"" + url.getProtocol	+ "\" is not supported.")
  }

  def getFileStream(file : File, lang : String = ContentTypes.langNQuad) = {

    log.info("Loading from " + file.getCanonicalPath)
    var inputStream:InputStream = null
    try {
      inputStream = new DecompressingStream(file).getStream
    } catch {
      case e:FileNotFoundException => {
        log.warn(file.getCanonicalPath + " vanished: " + e.getMessage)
        throw e
      }
    }

    inputStream = convertFormat(inputStream, lang)

    new BufferedInputStream(inputStream)
  }

  private def getUrlStream(url : URL, lang : String) = {

    log.info("Loading from " + url.toString)
    var inputStream:InputStream = null

    try  {
      inputStream = new DecompressingStream(url).getStream
    } catch {
      case e:Exception => {
        log.warn(url + " did not provide any data")
        throw e
      }
    }

    inputStream = convertFormat(inputStream, lang)

    new BufferedInputStream(inputStream)
  }

  // Convert input format to N-Triple
  private def convertFormat(inputStream : InputStream, lang : String) = {
    if (lang != ContentTypes.langNQuad && lang != ContentTypes.langNTriple)    {
      val runner = new Any23
      val source = new ByteArrayDocumentSource(inputStream, "http://www4.wiwiss.fu-berlin.de/ldif/", ContentTypes.getContentTypeFromLang(lang))
      val out = new ByteArrayOutputStream()
      val handler = new NTriplesWriter(out)
      runner.extract(source, handler)
      new ByteArrayInputStream(out.toByteArray)
    }
    else inputStream
  }

  def dumpIntoFileQuadQueue(sourceLocation: String): FileQuadReader = {
    val stream = getStream(sourceLocation)
    QuadFileLoader.loadQuadsIntoTempFileQuadQueue(stream)
  }
}
