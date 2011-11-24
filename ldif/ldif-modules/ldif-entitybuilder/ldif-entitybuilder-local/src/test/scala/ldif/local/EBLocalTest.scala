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

package ldif.local

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import java.util.Properties
import java.io.File
import runtime.impl.{FileEntityReader, FileEntityWriter, EntityQueue}
import runtime.{EntityReader, EntityWriter, ConfigParameters}

/**
 * Unit Test for the Entity Builder Module Local.
 */

@RunWith(classOf[JUnitRunner])
class EBLocalTest extends FlatSpec with ShouldMatchers
{
  testInMemory
  testInMemoryPlusFiles
  testTDB     // TODO - this test should pass

  // Test in memory execution
  def testInMemory  {
    println("Running in-memory test")
    // Create entity queues
    val eqs =
      for (ed <- TestUtils.eds) yield new EntityQueue(ed)

    // Configure the entity builder
    val prop = new Properties
    prop.setProperty("entityBuilderType","in-memory")

    // Run the entity builder
    runEB(prop, eqs)

    // Check results
    checkEntityQuantity(eqs, "in-memory")
    checkEntityQuality(eqs, "in-memory")

  }

  // Test in memory execution (write output to files)
  def testInMemoryPlusFiles {
    println("Running in-memory test (write output to files)")
    // Create entity queue writers
    val eqWriters =
      for(ed <- TestUtils.eds) yield {
        val file = File.createTempFile("ldif_entities", ".dat")
        file.deleteOnExit
        new FileEntityWriter(ed, file)
      }

    // Configure the entity builder
    val prop = new Properties
    prop.setProperty("entityBuilderType","in-memory")

    // Run the entity builder
    runEB(prop, eqWriters)

    // Create entity queue readers
    val eqReaders = eqWriters.map((entityWriter) =>
      new FileEntityReader(entityWriter.entityDescription, entityWriter.inputFile))

    // Check results
    checkEntityQuality(eqReaders, "in-memory+files")
  }

  // Test quad store execution (TDB)
  def testTDB {
    println("Running quad-store test")
    // Create entity queues
    val eqs =
      for (ed <- TestUtils.eds) yield new EntityQueue(ed)

    // Configure the entity builder
    val prop = new Properties
    prop.setProperty("entityBuilderType","quad-store")
    prop.setProperty("quadStoreType","tdb")
    //  prop.setProperty("databaseLocation", "/tmp/tdbtest")

    // Run the entity builder
    runEB(prop, eqs)

    // Check results
    checkEntityQuantity(eqs, "quad-store")
    checkEntityQuality(eqs, "quad-store")
  }


  def runEB (prop : Properties, eqs : IndexedSeq[EntityWriter]) {
    // Run the entity builder
    val ebe = new EntityBuilderExecutor(ConfigParameters(prop))
    ebe.execute(TestUtils.task, TestUtils.quads, eqs)
    // Give the eb some time..
    Thread.sleep(200)
  }


  def checkEntityQuantity (eqs : IndexedSeq[EntityReader], ebType : String)  {
    "EBLocal" should "create the correct number of entities "+ebType in  {
      eqs(0).size should equal (4)
      eqs(1).size should equal (9) // TODO: This fails for TDB. Why is this 9?? 1 seems to be correct. 9 seems to be the overall entity count. (because of no restriction of entity)
      eqs(2).size should equal (9)
      eqs(3).size should equal (4)
      eqs(4).size should equal (9)
      eqs(5).size should equal (9)
      eqs(6).size should equal (3)
    }
  }

  def checkEntityQuality (eqs : IndexedSeq[EntityReader], ebType : String)  {
    "EBLocal" should "retrieve the correct factums "+ebType in  {

      // EntityDescription(Restriction(Some(Condition(?SUBJ/rdf:type>,Set(<http://WhatEver>)))),
      // Vector(Vector()))
      while(eqs(0).hasNext){
        val entity = eqs(0).read
        entity.factums(0).size should equal (1)
        entity.factums(0).head.size should equal (0)
      }

      // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://someProp>)))
      while(eqs(1).hasNext){
        val entity = eqs(1).read
        if (entity.resource.value == "http://oooo") {
          val factums = entity.factums(0)
          factums.size should equal (2)
          val nodes = for (factum <- factums) yield
          {
            factum.size should equal (1)
            factum.head
          }

          val sortedNodes = nodes.toArray.sortBy(_.graph)

          sortedNodes.head.value should equal ("bla")
          sortedNodes.head.graph should equal ("someGraph")
          sortedNodes.last.value should equal ("blo")
          sortedNodes.last.graph should equal ("someOtherGraph")

        }
        else
          entity.factums(0).size should equal (0)
      }

      // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://n>, ?SUBJ/<http://v>)))
      while(eqs(2).hasNext){
        val entity = eqs(2).read
        if (entity.resource.value == "http://o1"){
          val factum = entity.factums(0).head
          // ?SUBJ/<http://n>
          factum(0).value should equal ("Locke")
          // ?SUBJ/<http://v>
          factum(1).value should equal ("John")
        }
        else
          entity.factums(0).size should equal (0)
      }

      //EntityDescription(Restriction(Some(Condition(?SUBJ/rdf:type,Set(<http://WhatEver>)))),
      // Vector(Vector(?SUBJ/<http://testNamespace/oldP>)))
      while(eqs(3).hasNext){
        val entity = eqs(3).read
        if (entity.resource.value == "http://testNamespace/resource2")
          entity.factums(0).head.head.value should equal ("same")
        else if (entity.resource.value == "http://testNamespace/resource1")
          entity.factums(0).head.head.value should equal ("same")
        else
          entity.factums(0).size should equal (0)
      }

      // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://a>/<http://c>/<http://d>)))
      while(eqs(4).hasNext){
        val entity = eqs(4).read
        if (entity.resource.value == "http://anotherPathResource") {
          entity.factums(0).size should equal (1)
          entity.factums(0).head.head.value should equal ("value even further away")
        }
        else
          entity.factums(0).size should equal (0)
      }

      // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://a>/<http://b>)))
      while(eqs(5).hasNext){
        val entity = eqs(5).read
        if (entity.resource.value == "http://pathResource") {
          entity.factums(0).size should equal (1)
          entity.factums(0).head.head.value should equal ("value at the end of the path")
        }
        else
          entity.factums(0).size should equal (0)
      }

      // EntityDescription(Restriction(Some(Condition(?SUBJ/<rdf:type>,Set(<http://testNamespace/someOldClass>)))),
      // Vector(Vector()))
      while(eqs(6).hasNext){
        val entity = eqs(6).read
        entity.factums(0).size should equal (1)
        entity.factums(0).head.size should equal (0)
      }

    }
  }
}

