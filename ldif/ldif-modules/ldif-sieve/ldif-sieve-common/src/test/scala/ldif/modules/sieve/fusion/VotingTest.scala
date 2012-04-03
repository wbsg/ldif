/*
 * LDIF
 *
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

package scala.ldif.modules.sieve.fusion

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import ldif.entity.Restriction.Condition
import ldif.util.Prefixes
import ldif.modules.sieve.fusion.functions.Voting
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.entity.{NodeTrait, Node, Path}
import ldif.modules.sieve.quality.HashBasedQualityAssessment

/**
 * Tests a fusion function
 */
@RunWith(classOf[JUnitRunner])
class VotingTest extends FlatSpec with ShouldMatchers {

  val fusionXml = <FusionFunction class="Voting"/>
  val votingFusionObj = new Voting;

  val prefixes = new Prefixes(Map(("rdfs", "http://example.com/rdfs"), ("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), ("dbpedia", "http://example.com/dbpedia"), ("dbpedia-owl", "http://example.com/dbpedia-owl"),("sieve", "http://sieve.wbsg.de/vocab/")))

  val classNode = Node.createUriNode(prefixes.resolve("dbpedia:Settlement"))
  val condition = new Condition(ldif.entity.Path.parse("?a/rdf:type")(prefixes), Set(classNode))

  /*
     Create three example nodes
   */
  val theCorrectValue= new Node("pickMe!", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph3")
  val theCorrectValueAgain = new Node("pickMe!", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph4")
  val lowestNode = new Node("node1", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph1")
  val nodes = IndexedSeq(lowestNode,
                         new Node("node2", "http://www.w3.org/2001/XMLSchema#dateTime", Node.TypedLiteral, "graph2"),
                         theCorrectValue,
                         theCorrectValueAgain)
  val patterns =  Traversable[IndexedSeq[NodeTrait]](nodes)


  /**
   * Create three dummy values for property p1
   */
  val quality = new HashBasedQualityAssessment()
  quality.putScore("p1","graph1",0.1)
  quality.putScore("p1","graph2",0.2)
  quality.putScore("p1","graph3",0.3)
  quality.putScore("p1","graph4",0.2)

  it should "create the correct fusion function from XML" in {
    (Voting.fromXML(fusionXml)(prefixes) equals votingFusionObj)
  }

  it should "correctly pick the most voted value" in {
    val fused = votingFusionObj.fuse(patterns,quality)
    println(fused)
    (fused equals theCorrectValue)
  }

  it should "fail" in {
    (true equals false)
  }

}


