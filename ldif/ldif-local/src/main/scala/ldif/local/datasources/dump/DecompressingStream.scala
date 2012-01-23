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

import java.io.{IOException, File, FileInputStream, InputStream}
import collection.immutable.HashMap
import java.util.zip.{GZIPInputStream, DeflaterInputStream}
import java.net.URL
import org.apache.tools.bzip2.CBZip2InputStream

/**
 * Wrapper class to permit transparent decompression of a dump files.
 * Based on <a href="http://mark.ossdl.de/2009/05/bzip2-library-for-java/">http://mark.ossdl.de/2009/05/bzip2-library-for-java/</a>
 */

@throws(classOf[IOException])
class DecompressingStream(inputStream:InputStream, fileName:String) {

  val compressionType = detectCompressionType(inputStream, fileName)

  object CompressionType extends Enumeration {
    val DEFLATE, GZIP, BZIP2 = Value
  }

  def encodingToCompressionType =
    HashMap("deflate" -> CompressionType.DEFLATE,
            "gzip" -> CompressionType.GZIP,
            "bzip" -> CompressionType.BZIP2)

  def contentTypeToCompressionType =
    HashMap("application/x-gzip" -> CompressionType.GZIP,
            "application/x-bzip" -> CompressionType.BZIP2)

  def extensionToCompressionType =
    HashMap(".gz" -> CompressionType.GZIP,
            ".bz2" -> CompressionType.BZIP2)

  // -- methods --

  def getStream = getWrappedStream(inputStream, compressionType)

  private def detectCompressionType(inputStream:InputStream, fileName:String) = {
    detectByContentExtension(fileName).orElse(detectByMagicByte(inputStream))
  }

  private def detectByContentExtension(fileName:String) = {
    var result:Option[CompressionType.Value] = None
    if (fileName != null) {
      for (e <- extensionToCompressionType.keySet) {
        if (fileName.toLowerCase.endsWith(e)) {
          result = extensionToCompressionType.get(e)
        }
      }
    }
    result
  }

  @throws(classOf[IOException])
  private def detectByMagicByte(inputStream:InputStream) : Option[CompressionType.Value] = {
    if (inputStream.markSupported) {
      inputStream.mark(3);

      val id1 = inputStream.read
      val id2 = inputStream.read
      val id3 = inputStream.read

      inputStream.reset()
      if ((id1 == 0x1f) && (id2 == 0x8b)) {
        Option(CompressionType.GZIP)
      } else if ((id1 == 0x42) && (id2 == 0x5a) && (id3 == 0x68)) {
        Option(CompressionType.BZIP2)
      } else None
    }
    else None
  }

  @throws(classOf[IOException])
  private def getWrappedStream(inputStream:InputStream, compressionType:Option[CompressionType.Value]):InputStream =
    compressionType match {
      case None => inputStream
      case Some(CompressionType.DEFLATE) => new DeflaterInputStream(inputStream)
      case Some(CompressionType.GZIP) => new GZIPInputStream(inputStream)
      case Some(CompressionType.BZIP2) => {
         /* CBZip2InputStream expects the magic number to be consumed */
         inputStream.read
         inputStream.read
         new CBZip2InputStream(inputStream)
      }
    }

  // -- constructors --

  @throws(classOf[IOException])
  def this(file:File) {
    this(new FileInputStream(file), file.getName)
  }

  @throws(classOf[IOException])
  def this(url:URL) {
    this(url.openStream, url.getFile)
  }

}

