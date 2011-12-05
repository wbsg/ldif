package ldif.local.runtime

import impl.QuadQueue
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.local.datasources.dump.QuadFileLoader
import java.io.{File, FileReader, BufferedReader}


/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/5/11
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */

@RunWith(classOf[JUnitRunner])
class UriTranslatorTest extends FlatSpec with ShouldMatchers {
  it should "generate the correct entity cluster out of the sameAs links" in {
    val inputQueue = generateInputQueue
    val uriMap = URITranslator.generateUriMap(inputQueue)
    uriMap.size should equal (correctUriMap.size)
    for((key, value) <- correctUriMap) {
      uriMap.get(key).get should equal (value)
    }
  }

  private def generateInputQueue: QuadReader = {
    val sameasFile = new File(getClass.getClassLoader.getResource("ldif/local/runtime/uri_translator_input.nq").toString.stripPrefix("file:"))
    println(sameasFile.getAbsolutePath)
    val inputFile = new BufferedReader(new FileReader(sameasFile))
    val quadQueue = new QuadQueue
    val parser = new QuadFileLoader("")
    parser.readQuads(inputFile, quadQueue)
    quadQueue
  }

  lazy val correctUriMap = Map (
    "http://linkeddata.uriburner.com/about/rdf/http://last.fm/music/Amanda+Miguel#this" -> "http://dbpedia.org/resource/Amanda_Miguel",
    "http://dbtune.org/musicbrainz/resource/artist/8ef7bbf6-04eb-43e4-ad3d-17cb8ad174b4" -> "http://dbpedia.org/resource/Amanda_Miguel",
    "http://linkeddata.uriburner.com/about/rdf/http://www.last.fm/music/Amanda+Miguel#this" -> "http://dbpedia.org/resource/Amanda_Miguel",
    "http://www.bbc.co.uk/music/artists/8ef7bbf6-04eb-43e4-ad3d-17cb8ad174b4#artist" -> "http://dbpedia.org/resource/Amanda_Miguel",
    "http://lastfm.rdfize.com/artists/Amanda+Miguel" -> "http://dbpedia.org/resource/Amanda_Miguel",
    "http://rdf.freebase.com/ns/en.amanda_miguel" -> "http://dbpedia.org/resource/Amanda_Miguel"
  )
}