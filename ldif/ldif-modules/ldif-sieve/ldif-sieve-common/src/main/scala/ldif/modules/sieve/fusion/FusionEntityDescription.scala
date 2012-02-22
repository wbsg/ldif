package ldif.modules.sieve.fusion

import ldif.util.Prefixes
import ldif.entity.{Path, Restriction, EntityDescription}

/**
 * Helper object to parse entity descriptions out of the Fusion Specification XML
 * @author pablomendes
 */

object FusionEntityDescription {

  /**
   * A <Class> node will be passed in.
   * Create a restriction for the Class (rdf:type)
   * For each sub-element Property, create a pattern.
   */
  def fromXML(classElement : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) = {
    // first grab the restriction on classNames
    val classNames = (classElement \ "@name").text.split(" ")
    // second grab the patterns from each Property element
//    val properties = (classElement \ "Property").map(getFusionFunction)
//    println(classNames,properties)

    //val entityDescriptionXml = EntityDescription(
    //  restriction= ,
    //  patterns=
    // ) //see EntityDescription.fromXML to understand how to create.
    //val e = EntityDescription.fromXML(entityDescriptionXml)(prefixes)

    //TODO temporary
    EntityDescription.fromXML(createSimpleEntityDescription)(prefixes)
  }

  def getFusionFunction(propertyNode: scala.xml.Node) = {
//    val propertyName = (propertyNode \ "@name").text
//    val className = (propertyNode \ "ScoringFunction" \ "@name").text
//    (propertyNode \ "ScoringFunction").map()
//    FusionFunction.create(className, _)
  }

  def main(args: Array[String]) {
        val fusionXml =
        <Class name="dbpedia:City">
            <Property name="dbpedia:areaTotal">
                <FusionFunction class="KeepValueWithHighestScore" metric="sieve:lastUpdated"/>
            </Property>
            <Property name="dbpedia:population">
                <FusionFunction class="AverageValue"/>
            </Property>
            <Property name="dbpedia:name">
                <FusionFunction class="KeepValueWithHighestScore" metric="sieve:reputation"/>
            </Property>
        </Class>

    fromXML(fusionXml)
  }
  /*
  def fromXML(scoringFunction : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) = {
    val paths = ((scoringFunction \ "ScoringFunction" \ "Input" ).map(parsePath(_)(prefixes)))
    val restriction = Restriction.fromXML(<Restriction><Condition path="?a/rdf:type"><Uri>http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph</Uri></Condition></Restriction>)
    new EntityDescription(restriction,IndexedSeq(paths.toIndexedSeq))
  }

  def parsePath(pathNode : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty)  = {
    Path.parse((pathNode \ "@path" ).text)(prefixes)
  } */

  def createSimpleEntityDescription = {
   <EntityDescription>
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


}