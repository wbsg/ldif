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
import org.slf4j.LoggerFactory
import java.io.OutputStream
import ldif.util._
import java.util.Properties

case class CsvImportJob(dumpLocation : String, id : Identifier, refreshSchedule : String, dataSource : String, fieldSeparator : String = Consts.DEFAULT_CSV_FIELD_SEPERATOR) extends DumpImportJob(dumpLocation) {

  val reporter = new CsvImportJobPublisher(id)

  override def getType = "csv"

  override def load(out : OutputStream, estimatedNumberOfQuads : Option[Double] = None) : Boolean = {
    importedGraphs += graph

    val parameters = new Properties()
    parameters.setProperty("jobId", id)
    parameters.setProperty("CsvFieldSeparator", fieldSeparator)

    loadDump(out, estimatedNumberOfQuads, parameters)
  }

  override def toXML = {
    val xml = {
      <csvImportJob>
        <dumpLocation>{dumpLocation}</dumpLocation>
        <fieldSeparator>{fieldSeparator}</fieldSeparator>
      </csvImportJob>
    }
    toXML(xml)
  }

  def getReporter = reporter
}

object CsvImportJob {

  /**
   * Creates a CsvImportJob from an XML definition
   * @param node Node
   * @param id Identifier
   * @param refreshSchedule String
   * @param dataSource String
   * @return CsvImportJob
   */
  def fromXML (node : Node, id : Identifier, refreshSchedule : String, dataSource : String) : ImportJob = {
    val dumpLocation : String = (node \ "dumpLocation" text)
    var fieldSeparator = (node \ "fieldSeparator" text)
    // When not defined, it returns an empty string
    if (fieldSeparator == "") {
      fieldSeparator = Consts.DEFAULT_CSV_FIELD_SEPERATOR
    }
    val job = new CsvImportJob(dumpLocation.trim, id, refreshSchedule, dataSource, fieldSeparator)
    job
  }
}

class CsvImportJobPublisher (id : Identifier) extends DumpImportJobPublisher(id) {
  override def getPublisherName = super.getPublisherName + " (csv)"
}