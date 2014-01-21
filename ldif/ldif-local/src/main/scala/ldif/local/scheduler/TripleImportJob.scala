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
import ldif.util._

case class TripleImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends DumpImportJob(dumpLocation) {

  val reporter = new TripleImportJobPublisher(id)

  override def getType = "triple"

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    importedGraphs += graph
    loadDump(out, estimatedNumberOfQuads)
  }

  override def toXML = {
    val xml = {
      <tripleImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
      </tripleImportJob>
    }
    toXML(xml)
  }

  def getReporter = reporter
}

object TripleImportJob {

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    val job = new TripleImportJob(dumpLocation.trim, id, refreshSchedule, dataSource)
    job
  }
}

class TripleImportJobPublisher (id : Identifier) extends DumpImportJobPublisher(id) {
  override def getPublisherName = super.getPublisherName + " (triple)"
}