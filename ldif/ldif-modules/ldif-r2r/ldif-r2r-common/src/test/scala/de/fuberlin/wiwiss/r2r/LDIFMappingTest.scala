/* 
 * LDIF
 *
 * Copyright 2011-2013 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

package de.fuberlin.wiwiss.r2r

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 19.05.11
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import ldif.local.runtime.impl.QuadQueue    //TODO remove ldif-local dependency
import ldif.runtime.Quad
import ldif.entity._
import CreatorHelperFunctions._

@RunWith(classOf[JUnitRunner])
class LDIFMappingTest extends FlatSpec with ShouldMatchers {
  val repository = new Repository(new FileOrURISource("ldif/modules/r2r/testMapping.ttl"))

  it should "be able to rename properties" in {
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/propertyRenamingMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("<TestURI1>", mapping)
    entity.addFactumRow(Node.createLiteral("testValue", "default"))
    val quadQueue = new QuadQueue
    mapping.executeMapping(entity, quadQueue)
    quadQueue.read.toString should equal ("Quad(<TestURI1>,p2,\"testValue\",default)")
  }

  it should "be able to convert URIs to Literals" in {
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/whateverToLiteralMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("<TestURI1>", mapping)
    entity.addFactumRow(Node.createUriNode("testValue", "default"))
    val quadQueue = new QuadQueue
    mapping.executeMapping(entity, quadQueue)
    quadQueue.read.toString should equal ("Quad(<TestURI1>,p2,\"testValue\",default)")
  }

  it should "pick the correct graph uri from the value nodes" in {
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/whateverToLiteralMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("<TestURI1>", mapping)
    entity.addFactumRow(Node.createUriNode("testValue", "myNameSpace"))
    val quadQueue = new QuadQueue
    mapping.executeMapping(entity, quadQueue)
    quadQueue.read.toString should equal ("Quad(<TestURI1>,p2,\"testValue\",myNameSpace)")
  }

  it should "generate blank nodes for the target graph" in {
    val mapping =  getMapping("http://mappings.dbpedia.org/r2r/blankNodeAndPathMapping", repository)
    val entityQueue = createEntityQueue(mapping.entityDescription)
    val entity = createEntity("<TestURI1>", mapping)
    entity.addFactumRow(Node.createLiteral("testValue", "myNameSpace"))
    val quadQueue = new QuadQueue
    mapping.executeMapping(entity, quadQueue)
    quadQueue.size should equal (2)
    val bn1 = quadQueue.read match{
      case Quad(_, _, bn, _) => bn }
    val bn2 = quadQueue.read match{
      case Quad(bn, _, _, _) => bn }
    bn1 should equal (bn2)
  }

}