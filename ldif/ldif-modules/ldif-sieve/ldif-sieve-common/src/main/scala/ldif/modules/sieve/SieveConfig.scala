package ldif.modules.sieve

import fusion.PassItOn
import ldif.util.Prefixes
import java.io.{FileInputStream, InputStream, File}
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 *
 * @author pablomendes
 */

class SieveConfig(val prefixes: Prefixes, val sieveSpecs: Traversable[FusionSpecification]) {

  def merge(c: SieveConfig) : SieveConfig = { //TODO implement
    throw new NotImplementedException
    //this
  }

}

object SieveConfig {

  val stdPrefixes = Map("dbpedia" -> "http://dbpedia.org/property/title/",
    "genes"->"http://wiking.vulcan.com/neurobase/kegg_genes/resource/vocab/",
    "smwprop"->"http://mywiki/resource/property/",
    "smwcat"->"http://mywiki/resource/category/",
    "wiki"->"http://www.example.com/smw#",
    "xsd" -> "http://www.w3.org/2001/XMLSchema#",
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#")

  def load(configFile: File) : SieveConfig = {
    load(new FileInputStream(configFile))
  }

  def load(configFile: InputStream) : SieveConfig = { //TODO implement config parser
    val spec1 = new FusionSpecification("title_passItOn", IndexedSeq(new PassItOn))
    val fusionSpecs = List(spec1)
    new SieveConfig(new Prefixes(stdPrefixes),fusionSpecs)
  }

  def empty = {
    new EmptySieveConfig
  }
}

class EmptySieveConfig extends SieveConfig(Prefixes.stdPrefixes, List(new FusionSpecification("Default", IndexedSeq(new PassItOn)))) {

}

//object LinkingConfig {
//  private val schemaLocation = "de/fuberlin/wiwiss/silk/LinkSpecificationLanguage.xsd"
//
//  def empty = LinkingConfig(Prefixes.empty, RuntimeConfig(), Nil, Nil, Nil)
//
//  def load = {
//    new ValidatingXMLReader(fromXML, schemaLocation)
//  }
//
//  def fromXML(node: Node) = {
//    implicit val prefixes = Prefixes.fromXML(node \ "Prefixes" head)
//    val sources = (node \ "DataSources" \ "DataSource").map(Source.fromXML)
//    val blocking = (node \ "Blocking").headOption match {
//      case Some(blockingNode) => Blocking.fromXML(blockingNode)
//      case None => Blocking()
//    }
//    val linkSpecifications = (node \ "Interlinks" \ "Interlink").map(p => LinkSpecification.fromXML(p))
//
//    implicit val globalThreshold = None
//    val outputs = (node \ "Outputs" \ "Output").map(Output.fromXML)
//
//    LinkingConfig(prefixes, RuntimeConfig(blocking), sources, linkSpecifications, outputs)
//  }
//}