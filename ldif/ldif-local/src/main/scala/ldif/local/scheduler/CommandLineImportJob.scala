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
import sys.process._

case class CommandLineImportJob(dumpLocation : String, scriptLocation : String, id : Identifier, refreshSchedule : String, dataSource : String) extends DumpImportJob(dumpLocation) {

  val reporter = new CommandLineImportJobPublisher(id)

  override def getType = "command-line-triple"

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    importedGraphs += graph
    ("bash " + scriptLocation).! // Execute the script
    loadDump(out, estimatedNumberOfQuads)
  }

  def toXML = {
    val xml = {
      <tripleImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
        <scriptLocation>{scriptLocation}</scriptLocation>
      </tripleImportJob>
    }
    toXML(xml)
  }

  def getReporter = reporter
}

object CommandLineImportJob {

  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    val scriptLocation : String = (node \ "scriptLocation" text)
    val job = new CommandLineImportJob(dumpLocation.trim, scriptLocation, id, refreshSchedule, dataSource)
    job
  }
}

class CommandLineImportJobPublisher (id : Identifier) extends DumpImportJobPublisher(id) {
  override def getPublisherName = super.getPublisherName + " (triple)"
}
