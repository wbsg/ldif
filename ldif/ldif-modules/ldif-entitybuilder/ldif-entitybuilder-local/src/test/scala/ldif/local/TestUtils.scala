/* 
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

package ldif.local

import datasources.dump.{QuadFileLoader, DumpLoader}
import ldif.entity.EntityDescription
import runtime.impl.BlockingQuadQueue
import xml.{XML, Source}
import ldif.util.{Prefixes, Consts}
import ldif.{EntityBuilderModule, EntityBuilderConfig}
import java.io.{InputStreamReader, BufferedReader, File}

object TestUtils {

  val sources = new File(getClass.getClassLoader.getResource("sources").getPath)

  val edPath = getClass.getClassLoader.getResource("entity_descriptions").getPath.toString + Consts.fileSeparator
  val eds = IndexedSeq (
    loadED(edPath + "ed0.xml"),
    loadED(edPath + "ed1.xml"),
    loadED(edPath + "ed2.xml"),
    loadED(edPath + "ed3.xml"),
    loadED(edPath + "ed4.xml"),
    loadED(edPath + "ed5.xml"),
    loadED(edPath + "ed6.xml"),
    loadED(edPath + "ed7.xml"))

  def loadEDs(sourceDir : String) : IndexedSeq[EntityDescription] = {
    val dir = new File (sourceDir)
    var eds = Seq.empty[EntityDescription]
    for (file <- dir.listFiles) {
      if (file.isDirectory)
        eds ++= loadEDs(file.getCanonicalPath)
      else eds :+= loadED(file.getCanonicalPath)
    }
    eds.toIndexedSeq
  }

  def loadED(sourcePath : String) : EntityDescription = {
    implicit val prefixes = Prefixes(
      Map("rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#"))
    val stream =  Source.fromFile(sourcePath).getByteStream
    EntityDescription.fromXML(XML.load(stream))
  }

  lazy val task = {
    val ebc = new EntityBuilderConfig(eds)
    val ebm = new EntityBuilderModule(ebc)
    // eb has only one task
    ebm.tasks.head
  }

  def quads =  {
    val quadQueues =
      for (dump <- sources.listFiles) yield {
        val quadQueue = new BlockingQuadQueue(Consts.DEFAULT_QUAD_QUEUE_CAPACITY)
        val inputStream = DumpLoader.getFileStream(dump)
        val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
        val quadParser = new QuadFileLoader(dump.getName)
        quadParser.readQuads(bufferedReader, quadQueue)
        quadQueue.finish
        quadQueue
      }
    quadQueues.toSeq
  }

}