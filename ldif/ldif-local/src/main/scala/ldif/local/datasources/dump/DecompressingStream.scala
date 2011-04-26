package ldif.local.datasources.dump

import java.io.{IOException, File, FileInputStream, InputStream}
import collection.immutable.HashMap
import java.util.zip.{GZIPInputStream, DeflaterInputStream}

//import org.apache.http.{Header, HttpEntity}
//import org.apache.tools.bzip2.CBZip2InputStream

/**
 * Wrapper class to permit transparent decompression of a dump files.
 * Based on <a href="http://mark.ossdl.de/2009/05/bzip2-library-for-java/">http://mark.ossdl.de/2009/05/bzip2-library-for-java/</a>
 */

@throws(classOf[IOException])
class DecompressingStream(inputStream:InputStream, fileName:String) extends InputStream{


  var wrappedStream = getWrappedStream(inputStream, detectByContentExtension(fileName))

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

  private def detectByEncoding(contentEncoding:String) = 
     encodingToCompressionType.get(contentEncoding)

  private def detectByContentType(contentType:String) =
		contentTypeToCompressionType.get(contentType)

  // private def detectByEncoding(contentEncoding:Header) = detectByEncoding(contentEncoding.getValue())

	// private def detectByContentType(contentType:Header) = detectByContentType(contentType.getValue())


  private def detectByContentExtension(fileName:String) = {
		var result:Option[CompressionType.Value] = None
    if (fileName != null) {
			for (e <- extensionToCompressionType.keySet) {
			  if (fileName.toLowerCase().endsWith(e)) {
		  		result = extensionToCompressionType.get(e)
		  	}
      }
    }
		result
	}

  @throws(classOf[IOException])
  private def detectByMagicByte(inputStream:InputStream) = {
    var result:CompressionType.Value = null
    if (inputStream.markSupported) {
      inputStream.mark(3);

      val id1 = inputStream.read
      val id2 = inputStream.read
      val id3 = inputStream.read

      inputStream.reset
      if ((id1 == 0x1f) && (id2 == 0x8b)) {
        result = CompressionType.GZIP
      } else if ((id1 == 0x42) && (id2 == 0x5a) && (id3 == 0x68)) {
        result = CompressionType.BZIP2
      }
    }
    result
}

  @throws(classOf[IOException])
  private def getWrappedStream(inputStream:InputStream, compressionType:Option[CompressionType.Value]):InputStream =
    compressionType match {
        case None => inputStream
        case CompressionType.DEFLATE => new DeflaterInputStream(inputStream)
        case CompressionType.GZIP => new GZIPInputStream(inputStream)
        // TODO requires a lib dependency (bzip2) which is not in the maven repository
        // case CompressionType.BZIP2 => {
        //   /* CBZip2InputStream expects the magic number to be consumed */
        //   inputStream.read()
        //   inputStream.read()
        //   new CBZip2InputStream(inputStream)
        // }
    }

  @throws(classOf[IOException])
	override def read =	wrappedStream.read 


  // -- constructors --

  @throws(classOf[IOException])
	def this(file:File) {
    this(new FileInputStream(file), file.getName())
  }

  //  @throws(classOf[IOException])
  //  def this(entity:HttpEntity) {
  //		val contentEncoding:Header = entity.getContentEncoding
  //		val contentType:Header = entity.getContentType
  //		val inputStream:InputStream = entity.getContent
  //
  //		var compressionType = detectByEncoding(contentEncoding)
  //		if (compressionType = None) {
  //			compressionType = detectByContentType(contentType)
  //		}
  //
  //		if (compressionType = None) {
  //			compressionType = detectByMagicByte(inputStream)
  //		}
  //
  //		wrappedStream = getWrappedStream(inputStream, compressionType)
  //	}

}

