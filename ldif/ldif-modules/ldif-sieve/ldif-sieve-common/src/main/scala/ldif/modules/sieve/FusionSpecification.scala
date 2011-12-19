/* 
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG 
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

import fusion.FusionFunction

/**
 * 
 * @author pablomendes
 */
class FusionSpecification(val id: String, val fusionFunctions : IndexedSeq[FusionFunction], val outputPropertyNames: IndexedSeq[String] ) {
    //val fusionFunctions = new PassItOn
    //val fusionFunctions = new KeepFirst
    //val fusionFunctions = new TrustYourFriends("http://www4.wiwiss.fu-berlin.de/ldif/graph#dbpedia.en");
}