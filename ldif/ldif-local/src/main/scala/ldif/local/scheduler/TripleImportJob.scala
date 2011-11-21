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

package ldif.local.scheduler

import ldif.local.datasources.dump.DumpLoader
import xml.Node
import ldif.datasources.dump.QuadParser
import java.io.{OutputStreamWriter, OutputStream}
import ldif.util.{Consts, Identifier}

case class TripleImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends ImportJob {
  val graph = Consts.DEFAULT_IMPORTED_GRAPH_PREFIX+id

  override def load(out : OutputStream) : Boolean = {

    val writer = new OutputStreamWriter(out)

    // get bufferReader from Url
    val inputStream = DumpLoader.getStream(dumpLocation)
    //val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    importedGraphs += graph

    val parser = new QuadParser(graph)
    val lines = scala.io.Source.fromInputStream(inputStream).getLines
    for (line <- lines.toTraversable){
        val quad = parser.parseLine(line)
        if (quad != null)
          writer.write(quad.toNQuadFormat+". \n")
    }
    writer.flush
    writer.close
    true
  }

  override def getType = "triple"
  override def getOriginalLocation = dumpLocation
}

object TripleImportJob {

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation") text
    val job = new TripleImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}