/* 
 * LDIF
 *
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

package ldif.local.runtime.impl

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.local.datasources.sparql.SparqlExecutor
import ldif.datasources.sparql.{SparqlModule, SparqlConfig}
import xml.XML
import ldif.util.Prefixes
import ldif.entity.EntityDescription

@RunWith(classOf[JUnitRunner])
class SparqlExecutorTest extends FlatSpec with ShouldMatchers {

  val sourceUrl = "http://cheminfov.informatics.indiana.edu:8080/pharmgkb/sparql"

  val executor = new SparqlExecutor
  val eq = task.entityDescriptions.map(new EntityQueue(_))

  /* Disabled - remote test */

//  it should "build entities correctly" in {
//    executor.execute(task,null,eq)
//    eq.head.size should equal (1000)
//  }

  private lazy val task = {
    val config = new SparqlConfig(Traversable(sourceUrl), IndexedSeq(ed("pharmGKB_ed.xml")))
    val module = new SparqlModule(config)
    module.tasks.head
  }

  private def ed(sourceUrl : String) = {

    implicit val prefixes = Prefixes(Map(
      "rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "pharmGKB" -> "http://chem2bio2rdf.org/pharmgkb/resource/"))

    val stream = getClass.getClassLoader.getResourceAsStream(sourceUrl)

    EntityDescription.fromXML(XML.load(stream))
  }

}