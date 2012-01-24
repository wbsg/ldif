package ldif.modules.sieve.fusion

import xml.Elem
import ldif.util.Prefixes

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
  def fromXML(classElement : Elem)(implicit prefixes : Prefixes = Prefixes.empty) = {
    // first grab the restriction
    // val classRestriction = ...
    // second grab the patterns from each Property element
    //val properties = (classElement \ "Property" ).map(grabPropertyName)
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