/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

import ldif.local.datasources.dump.DumpLoader
import xml.Node
import ldif.datasources.dump.QuadParser
import java.io.{OutputStreamWriter, OutputStream}
import ldif.util.{Consts, Identifier}

case class QuadImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {

  override def load(out : OutputStream) : Boolean = {

    val writer = new OutputStreamWriter(out)

    // get bufferReader from Url
    val inputStream = DumpLoader.getStream(dumpLocation)

    val parser = new QuadParser
    val lines = scala.io.Source.fromInputStream(inputStream).getLines
    for (line <- lines.toTraversable){
        val quad = parser.parseLine(line)
        importedGraphs += quad.graph
        writer.write(quad.toNQuadFormat+" . \n")
        if (importedGraphs.size >= Consts.MAX_NUM_GRAPHS_IN_MEMORY)
          writeImportedGraphsToFile
    }
    writer.flush
    writer.close
    true
  }

  override def getType = "quad"
  override def getOriginalLocation = dumpLocation
}

object QuadImportJob{

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    val job = new QuadImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}