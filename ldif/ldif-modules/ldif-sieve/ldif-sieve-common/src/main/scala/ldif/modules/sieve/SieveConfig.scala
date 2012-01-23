/* 
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

package ldif.modules.sieve

import fusion.{KeepUpToDate, TrustYourFriends, PassItOn}
import ldif.util.Prefixes
import java.io.{FileInputStream, InputStream, File}
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import ldif.entity.EntityDescription
import org.slf4j.LoggerFactory

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

  private val log = LoggerFactory.getLogger(getClass.getName)

  val stdPrefixes = Map("foaf" -> "http://xmlns.com/foaf/0.1/",
    "dbpedia-owl" -> "http://dbpedia.org/ontology/",
    //"dbpedia" -> "http://dbpedia.org/resource/",
    "genes"->"http://wiking.vulcan.com/neurobase/kegg_genes/resource/vocab/",
    "smwprop"->"http://mywiki/resource/property/",
    "smwcat"->"http://mywiki/resource/category/",
    "wiki"->"http://www.example.com/smw#",
    "ldif"->"http://www4.wiwiss.fu-berlin.de/ldif/",
    "xsd" -> "http://www.w3.org/2001/XMLSchema#",
    "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    "rdfs" -> "http://www.w3.org/2000/01/rdf-schema#",
    "owl" -> "http://www.w3.org/2002/07/owl#")

  def load(configFile: File) : SieveConfig = {
    load(new FileInputStream(configFile))
  }

  /*
<http://dbpedia.org/ontology/musicalArtist>
<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
<http://www.w3.org/2000/01/rdf-schema#label>
<http://www.w3.org/2002/07/owl#sameAs>
<http://xmlns.com/foaf/0.1/made>
<http://xmlns.com/foaf/0.1/member>

provenance triples:
<http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob>
<http://www4.wiwiss.fu-berlin.de/ldif/hasImportType>
<http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation>
<http://www4.wiwiss.fu-berlin.de/ldif/importId>
<http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate>

   */
  def load(configFile: InputStream) : SieveConfig = { //TODO implement config parser
    val fusionSpecs = createLwdm2012ExampleSpecs
    new SieveConfig(new Prefixes(stdPrefixes),fusionSpecs)
  }

  def createLwdm2012ExampleSpecs = {
    val spec1 = new FusionSpecification("lwdm2012",
                      IndexedSeq(new PassItOn,
                                 new TrustYourFriends("http://en.wikipedia.org.+"),
                                 new PassItOn,
                                 new KeepUpToDate("http://www4.wiwiss.fu-berlin.de/ldif/lastUpdate"),
                                 new PassItOn),
                      IndexedSeq("http://www.w3.org/2000/01/rdf-schema#label",
                                 "http://dbpedia.org/ontology/areaTotal",
                                 "http://dbpedia.org/ontology/foundingDate",
                                 "http://dbpedia.org/ontology/populationTotal",
                                 "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
    )
    List(spec1)
  }

   def createLwdm2012EntityDescription = {
    <EntityDescription>
      <Restriction>
        <Condition path="?a/rdf:type">
          <Uri>http://dbpedia.org/ontology/Settlement</Uri>
        </Condition>
        <Condition path="?a/dbpedia-owl:country">
          <Uri>http://dbpedia.org/resource/Brazil</Uri>
        </Condition>
      </Restriction>
      <Patterns>
        <Pattern>
          <Path>?a/rdfs:label</Path>
        </Pattern>
        <Pattern>
          <Path>?a/dbpedia-owl:areaTotal</Path>
        </Pattern>
        <Pattern>
          <Path>?a/dbpedia-owl:foundingDate</Path>
        </Pattern>
        <Pattern>
          <Path>?a/dbpedia-owl:populationTotal</Path>
        </Pattern>
        <Pattern>
          <Path>?a/rdfs:type</Path>
        </Pattern>
      </Patterns>
    </EntityDescription>
  }

  def createDummyEntityDescriptions(prefixes: Prefixes) : List[EntityDescription] = {
    // read from jar
    //val stream = getClass.getClassLoader.getResourceAsStream("ldif/modules/sieve/local/Music_EntityDescription.xml")
    // read from file
    //val stream = new FileInputStream("/home/pablo/workspace/ldif/ldif/ldif-modules/ldif-sieve/ldif-sieve-local/src/test/resources/ldif/modules/sieve/local/Music_EntityDescription.xml");

    //if (stream!=null) {
//      val testXml = XML.load(stream);
      val testXml = createLwdm2012EntityDescription

      val e = EntityDescription.fromXML(testXml)(prefixes)
      log.debug("[FUSION] "+e.toString);
      List(e)
//    } else {
//      log.error("EntityDescription returned null!");
//      List() //empty?
//    }
  }

  def createMusicExampleSpecs = {
    val spec1 = new FusionSpecification("test",
                      IndexedSeq(new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en"), new PassItOn, new PassItOn, new PassItOn),
                      IndexedSeq("http://www.w3.org/2000/01/rdf-schema#label", "http://xmlns.com/foaf/0.1/made", "http://www.w3.org/2002/07/owl#sameAs", "http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource")
    )
    List(spec1)
  }

  def createMusicExampleDescription = {
    <EntityDescription>
        <Patterns>
          <Pattern>
            <Path>?a/rdfs:label</Path>
          </Pattern>
          <Pattern>
            <Path>?a/foaf:made</Path>
          </Pattern>
          <Pattern>
            <Path>?a/owl:sameAs</Path>
          </Pattern>
          <Pattern>
            <Path>?a/ldif:hasDatasource</Path>
          </Pattern>
        </Patterns>
      </EntityDescription>
  }

  def empty : EmptySieveConfig = {
    new EmptySieveConfig
  }
}

/*
 This class should never be actually used for fusion. It simply signals that no config exists, and the framework should repeat the input.
 */
class EmptySieveConfig extends SieveConfig(Prefixes.stdPrefixes, List(new FusionSpecification("Default", IndexedSeq(new PassItOn), IndexedSeq("DEFAULT")))) {

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