package ldif.modules.sieve

import java.io.{InputStream, File}
import ldif.util.Prefixes

/**
 * 
 * @author pablomendes
 */

class SieveConfig(val prefixes: Prefixes, val sieveSpecs: Traversable[FusionSpecification]) {

    def merge(c: SieveConfig) : SieveConfig = { //TODO implement
        this
    }

}

object SieveConfig {
    val stdPrefixes = Map("genes"->"http://wiking.vulcan.com/neurobase/kegg_genes/resource/vocab/",
        "smwprop"->"http://mywiki/resource/property/",
        "smwcat"->"http://mywiki/resource/category/",
        "wiki"->"http://www.example.com/smw#",
        "xsd" -> "http://www.w3.org/2001/XMLSchema#",
        "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
        "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#")

    def load(configFile: File) : SieveConfig = { //TODO implement

        new SieveConfig(new Prefixes(stdPrefixes),List(new FusionSpecification("test")))
    }

    def load(configFile: InputStream) : SieveConfig = { //TODO implement
        new SieveConfig(new Prefixes(stdPrefixes),List(new FusionSpecification("test")))
    }

    def empty = {
        new SieveConfig(Prefixes.stdPrefixes, List())
    }
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