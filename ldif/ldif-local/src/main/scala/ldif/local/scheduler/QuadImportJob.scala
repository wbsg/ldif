/* 
 * LDIF
 *
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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

package ldif.local.scheduler

import xml.Node
import java.io.OutputStream
import ldif.runtime.Quad
import ldif.util._

case class QuadImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String, renameGraphs : String = "") extends DumpImportJob(dumpLocation) {

  val reporter = new QuadImportJobPublisher(id)

  override def getType = "quad"

  def isRenameGraphEnabled = renameGraphs != ""

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    loadDump(out, estimatedNumberOfQuads, processQuad = processQuad)
  }

  /**
   * Function to be applied to each single quad, while loading the dump.
   * Since this applies to each loaded quad, it's here important to use less memory as possible.
   */
  def processQuad (quad : Quad) : Quad = {
    var renamedGraph : String = null

    // Add graph to imported graph list
    if (isRenameGraphEnabled) {
      renamedGraph = renameGraph(quad.graph)
      importedGraphs += renamedGraph
    } else {
      importedGraphs += quad.graph
    }

    // Evaluate dumping imported graph list to file
    if (importedGraphs.size >= Consts.MAX_NUM_GRAPHS_IN_MEMORY) {
      writeImportedGraphsToFile
    }

    // Return correct quad
    if (isRenameGraphEnabled) {
      quad.copy(graph = renamedGraph)
    } else {
      quad
    }
  }

  /* Rename graph according to a given regex */
  protected def renameGraph(from : String, to : String = "") : String = {
    //    from match {
    //      case re(g, sub) => g
    //      case _ => from
    //    }
    from.replaceAll(renameGraphs, to)
  }

  def toXML = {
    val xml = {
      <quadImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
        {if(isRenameGraphEnabled) <renameGraphs>{renameGraphs}</renameGraphs>}
      </quadImportJob>
    }
    toXML(xml)
  }

  def getReporter = reporter
}

object QuadImportJob{

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    val renameGraphs = (node \ "renameGraphs" text)
    val job = new QuadImportJob(dumpLocation.trim, id, refreshSchedule, dataSource, renameGraphs)
    job
  }
}

class QuadImportJobPublisher (id : Identifier) extends DumpImportJobPublisher(id) {
  override def getPublisherName = super.getPublisherName + " (quad)"
}