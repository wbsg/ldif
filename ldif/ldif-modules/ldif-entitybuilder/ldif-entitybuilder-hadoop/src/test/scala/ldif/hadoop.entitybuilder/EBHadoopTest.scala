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

package ldif.hadoop

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import ldif.hadoop.entitybuilder.phases._
import java.io.File
import xml.{XML, Source}
import ldif.util.{Consts, Prefixes}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{IntWritable, SequenceFile}
import ldif.entity.{Entity, EntityWritable, EntityDescription, EntityDescriptionMetaDataExtractor}

/**
 * Unit Test for the Hadoop Entity Builder Module.
 */

@RunWith(classOf[JUnitRunner])
class EBHadoopTest extends FlatSpec with ShouldMatchers
{
  // Create input structures
  val resourceDir = getClass.getClassLoader.getResource("hadoop").getPath.toString
  val sourcesDir = resourceDir+Consts.fileSeparator+"sources"
  val outputDirPrefix = resourceDir+Consts.fileSeparator+"output_phase"
  val edDir = resourceDir+Consts.fileSeparator+"entity_descriptions"+Consts.fileSeparator
  val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)

  // Run entity builder
  Phase2.runPhase(sourcesDir, outputDirPrefix+"/eb/phase2", edmd)
  Phase3.runPhase(outputDirPrefix+"/eb/phase2", outputDirPrefix+"/eb/phase3", edmd)
  Phase4.runPhase(outputDirPrefix+"/eb/phase3", outputDirPrefix+"/eb/output", edmd)

  // Check results
  val eqs = readOuputFiles
  "EBHadoop" should "create the correct number of entities" in  {

    //for ((eq,i) <- eqs.zipWithIndex) println(eq.size +" <- " +entityDescriptions(i).toString)

    eqs(0).size should equal (4)
    eqs(1).size should equal (1)
    eqs(2).size should equal (1)
    eqs(3).size should equal (2)
    eqs(4).size should equal (1)
    eqs(5).size should equal (1)
    eqs(6).size should equal (3)

  }

  "EBHadoop" should "retrieve the correct factums " in  {

    // EntityDescription(Restriction(Some(Condition(?SUBJ/rdf:type>,Set(<http://WhatEver>)))),
    // Vector(Vector()))
    for(entity <- eqs(0)){
      entity.factums(0).size should equal (1)
      entity.factums(0).head.size should equal (0)
    }

    // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://someProp>)))
    for(entity <- eqs(1)){
      if (entity.resource.value == "http://oooo") {
        val factums = entity.factums(0)
        factums.size should equal (2)
        val nodes = for (factum <- factums) yield
        {
          factum.size should equal (1)
          factum.head
        }
        val sortedNodes = nodes.toArray.sortBy(_.value)

        sortedNodes.head.value should equal ("bla")
        sortedNodes.head.graph should equal ("someGraph")
        sortedNodes.last.value should equal ("blo")
        sortedNodes.last.graph should equal ("someOtherGraph")
      }
      else
        entity.factums(0).size should equal (0)
    }

    // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://n>, ?SUBJ/<http://v>)))
    for(entity <- eqs(2)){
      if (entity.resource.value == "http://o1"){
        val factum = entity.factums(0).head
        // ?SUBJ/<http://n>
        factum(0).value should equal ("John")
        // ?SUBJ/<http://v>
        factum(1).value should equal ("Locke")
      }
      else
        entity.factums(0).size should equal (0)
    }

    //EntityDescription(Restriction(Some(Condition(?SUBJ/rdf:type,Set(<http://WhatEver>)))),
    // Vector(Vector(?SUBJ/<http://testNamespace/oldP>)))
    for(entity <- eqs(3)){
      if (entity.resource.value == "http://testNamespace/resource2")
        entity.factums(0).head.head.value should equal ("same")
      else if (entity.resource.value == "http://testNamespace/resource1")
        entity.factums(0).head.head.value should equal ("same")
      else
        entity.factums(0).size should equal (0)
    }

    // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://a>/<http://c>/<http://d>)))
    for(entity <- eqs(4)){
      if (entity.resource.value == "http://anotherPathResource") {
        entity.factums(0).size should equal (1)
        entity.factums(0).head.head.value should equal ("value even further away")
      }
      else
        entity.factums(0).size should equal (0)
    }

    // EntityDescription(Restriction(None),Vector(Vector(?SUBJ/<http://a>/<http://b>)))
    for(entity <- eqs(5)){
      if (entity.resource.value == "http://pathResource") {
        entity.factums(0).size should equal (1)
        entity.factums(0).head.head.value should equal ("value at the end of the path")
      }
      else
        entity.factums(0).size should equal (0)
    }

    // EntityDescription(Restriction(Some(Condition(?SUBJ/<rdf:type>,Set(<http://testNamespace/someOldClass>)))),
    // Vector(Vector()))
    for(entity <- eqs(6)){
      entity.factums(0).size should equal (1)
      entity.factums(0).head.size should equal (0)
    }
  }


  // Utils

  lazy val entityDescriptions = IndexedSeq (
    loadED(edDir + "ed0.xml"),
    loadED(edDir + "ed1.xml"),
    loadED(edDir + "ed2.xml"),
    loadED(edDir + "ed3.xml"),
    loadED(edDir + "ed4.xml"),
    loadED(edDir + "ed5.xml"),
    loadED(edDir + "ed6.xml"))

  def loadED(sourcePath : String) : EntityDescription = {
    implicit val prefixes = Prefixes(
      Map("rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
    val stream =  Source.fromFile(sourcePath).getByteStream
    EntityDescription.fromXML(XML.load(stream))
  }

  def readOuputFiles = {
    val config = new Configuration
    val fileSystem = FileSystem.get(config)

    val outputFiles = loadOutput(new File(outputDirPrefix+"/eb/output"))
    val eqs = outputFiles.map(_ => Seq.empty[Entity]).toArray

    for (file <- outputFiles)  {
      val reader = new SequenceFile.Reader(fileSystem, file, config)
      val kk = reader.getKeyClass.newInstance.asInstanceOf[IntWritable]
      val vv = reader.getValueClass.newInstance.asInstanceOf[EntityWritable]
      while (reader.next(kk, vv)) {
        eqs(kk.get) :+= vv
        // println( kk +" ) " +vv.resource.value +" \n"+ vv + "\n-----------------")
      }
      reader.close
    }

    eqs
  }

  def loadOutput(dir : File) : Seq[Path]= {
    var seqFiles = Seq.empty[Path]
    for (file <- dir.listFiles) {
      if (file.isDirectory)
        seqFiles ++= loadOutput(file)
      else if (file.getName.startsWith("part"))
        seqFiles :+= new Path(file.getCanonicalPath)
    }
    seqFiles.toIndexedSeq
  }
}