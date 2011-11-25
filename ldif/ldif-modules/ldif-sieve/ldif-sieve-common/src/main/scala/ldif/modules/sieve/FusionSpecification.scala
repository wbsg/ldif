package ldif.modules.sieve

import fusion.FusionFunction

/**
 * 
 * @author pablomendes
 */
class FusionSpecification(val id: String, val fusionFunctions : IndexedSeq[FusionFunction] ) {
    //val fusionFunctions = new PassItOn
    //val fusionFunctions = new KeepFirst
    //val fusionFunctions = new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
}