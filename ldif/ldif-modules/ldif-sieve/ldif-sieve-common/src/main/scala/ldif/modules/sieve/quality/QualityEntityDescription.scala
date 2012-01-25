package ldif.modules.sieve.quality

import ldif.util.Prefixes
import ldif.entity.EntityDescription

/**
 * Helper object to create entity descriptions out of the Quality Specification XML
 * Since indicators come from graphs,
 * Create one entity description from all assessment metrics.
 *
 * @author pablomendes
 */

object QualityEntityDescription {

  /**
   * A <Quality> node will be passed in
   * Need to grab pattern from all Input elements
   * TODO Initially consider only @path, later also need to consider @query
   */
  def fromXML(classElement : scala.xml.Node)(implicit prefixes : Prefixes = Prefixes.empty) = {
    // second grab the patterns from each Input element
    //val properties = (classElement \ "AssessmentMetric" \ "ScoringFunction" \ "Input" ).map(grabPathOrQuery)
    //val entityDescriptionXml = EntityDescription(
    //  restriction= ,
    //  patterns=
    // ) //see EntityDescription.fromXML to understand how to create.
    //val e = EntityDescription.fromXML(entityDescriptionXml)(prefixes)

    EntityDescription.fromXML(createLwdm2012EntityDescription)(prefixes)
  }

//  ldif:lastUpdate
  def createLwdm2012EntityDescription = {
   <EntityDescription>
     <Restriction>
     </Restriction>
     <Patterns>
       <Pattern>
         <Path>?a/rdf:type</Path>
       </Pattern>
     </Patterns>
   </EntityDescription>
 }

}