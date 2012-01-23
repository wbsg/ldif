package ldif.modules.sieve

import fusion.{PassItOn, FusionFunction}

/**
 * 
 * @author pablomendes
 */
class FusionSpecification(val id: String, val fusionFunctions : IndexedSeq[FusionFunction], val outputPropertyNames: IndexedSeq[String], val defaultFusionFunction : FusionFunction = new PassItOn) {

  assert(fusionFunctions.size==outputPropertyNames.size, "There should be one OutputPropertyName for each FusionFunction")
    //val fusionFunctions = new PassItOn
    //val fusionFunctions = new KeepFirst
    //val fusionFunctions = new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
}