package ldif.local.datasources.dump

import java.net.URL
import java.io.{IOException, FileNotFoundException, InputStream, File}
import java.util.logging.Logger

/**
 * Streams data from a given file path or URL
 * Accepts N-TRIPLE, N3 and RDF/XML serializations and gzip, bz2 and zip compression.
**/

@throws(classOf[Exception])
class DumpLoader(dataLocationUrl:String) {
  private val log = Logger.getLogger(getClass.getName)

  def getStream = {
    var inputStream:InputStream = null
    
    if (dataLocationUrl == null) {
			throw new Exception("Invalid data location" )
		}

    var url:URL = null
    var file:File = null

    try {
      url = new URL(dataLocationUrl);
    } catch {
        case e:Exception => {
          file = new File(dataLocationUrl);
          if (!file.exists()) {
            throw new Exception("File does not exist: "	+ e.getMessage())
          }
        }
    }

    if (url != null && url.getProtocol().toLowerCase().equals("file")) {
        file = new File(url.getFile())
      }

    if (file != null) {
			val lang = ContentTypes.jenaLangFromExtension(file.getName())

			if (lang == null) {
        throw new Exception("Unable to determine language for "+ dataLocationUrl	+ " based on file extension")
			}

      log.info("Loading from " + file.getAbsolutePath() + " using language " + lang)

     	try {
				inputStream = new DecompressingStream(file)
        } catch {
          case e:FileNotFoundException => {
        		log.warning(file.getAbsolutePath() + " vanished: " + e.getMessage())
            throw e;
			    }
          case e:IOException => {
				    log.warning("IO exception: " + e.getMessage())
            throw e;
		    	}
		  }

    } else if (url != null) {
      log.info("Loading from " + url)

      // TODO load from url
    }
    inputStream
  }

}
