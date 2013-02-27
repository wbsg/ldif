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

package ldif.local

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scheduler._
import ldif.config.SchedulerConfig
import ldif.util.{Consts, OutputValidator, CommonUtils}

@RunWith(classOf[JUnitRunner])
class SchedulerTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.getFileFromPath("scheduler/scheduler-config.xml")
  val scheduler = Scheduler(SchedulerConfig.load(configFile))

  it should "schedule a job correctly" in {
    val importJobs = scheduler.getImportJobs
    if(importJobs.head.id=="test_local")
      scheduler.checkUpdate(scheduler.getImportJobs.head) should equal (true)
    else
      scheduler.checkUpdate(scheduler.getImportJobs.tail.head) should equal (true)
  }

	it should "parse a job configuration correctly" in {
		val importJobs = scheduler.getImportJobs.toList.sortBy(_.id.toString)
		// Compare import jobs lists size
		importJobs.size should equal (correctImportJobs.size)
		// Compare import jobs lists content
		importJobs.corresponds(correctImportJobs) {_ == _} should equal (true)
	}

  it should "load a N-Quads dump and provenance correctly" in {
    // Run local import jobs
    scheduler.evaluateImportJobs
    // Wait for the import jobs to be completed
    Thread.sleep(1000)

    val dumpDir = new File(scheduler.config.dumpLocationDir)
    val dumpFile = dumpDir.listFiles().filter(_.getName.equals("test_local.nq")).head
    val dumpQuads = CommonUtils.getQuads(dumpFile)

    dumpQuads.size should equal (8)

    val dumpCorrectQuads = CommonUtils.getQuads(List(
      "<http://source/graph1> <http://ldif/provProp>  \"_\" <http://ldif/provGraph> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph1> . ",
      "<http://source/uriA> <http://source/mintProp> \"mint\" <http://source/graph2> . ",
      "<http://source/uriA> <http://source/mapProp> \"map\" <http://source/graph3> . ",
      "<http://source/uriB> <http://source/mapProp> \"map\" <http://source/graph4> . ",
      "<http://source/uriD> <http://source/mintProp> \"mintNotMapped\" <http://source/graph5> . ",
      "<http://source/uriA> <"+Consts.RDFTYPE_URI+"> <http://source/class> <http://source/graph6>. ",
      "<http://source/uriB> <"+Consts.RDFTYPE_URI+"> <http://source/class> <http://source/graph7> . "
    ))
    OutputValidator.contains(dumpQuads, dumpCorrectQuads) should equal(true)

    val provenanceFile = dumpDir.listFiles().filter(_.getName.equals("test_local.provenance.nq")).head
    val provenanceQuads = CommonUtils.getQuads(provenanceFile)

    provenanceQuads.size should equal (23)

    val provCorrectQuads = CommonUtils.getQuads(List(
      "_:test5Flocal <"+Consts.RDFTYPE_URI+"> <"+Consts.importJobClass+"> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> . ",
      "_:test5Flocal <"+Consts.importIdProp+"> \"test_local\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "_:test5Flocal <"+Consts.hasDatasourceProp+"> \"test\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "_:test5Flocal <"+Consts.hasImportTypeProp+"> \"quad\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "_:test5Flocal <"+Consts.numberOfQuadsProp+"> \"31\"^^<"+Consts.xsdNonNegativeInteger+"> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "_:test5Flocal <"+Consts.hasOriginalLocationProp+"> \"scheduler/sources/source.nq\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "<http://source/graph3> <"+Consts.hasImportJobProp+"> _:test5Flocal <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "<http://source/graph3> <"+Consts.RDFTYPE_URI+"> <"+Consts.importedGraphClass+"> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "<http://source/graph1> <"+Consts.hasImportJobProp+"> _:test5Flocal <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "<http://source/graph1> <"+Consts.RDFTYPE_URI+"> <"+Consts.importedGraphClass+"> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
      "<http://source/graph6> <"+Consts.hasImportJobProp+"> _:test5Flocal <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> ."
    ))
    OutputValidator.contains(provenanceQuads, provCorrectQuads) should equal(true)
  }

	it should "load a CSV dump and provenance correctly" in {
		// Run local import jobs
		scheduler.evaluateImportJobs
		// Wait for the import jobs to be completed
		Thread.sleep(1000)

		val dumpDir = new File(scheduler.config.dumpLocationDir)
		val dumpFile = dumpDir.listFiles().filter(_.getName.equals("test_local_csv.nq")).head
		val dumpQuads = CommonUtils.getQuads(dumpFile)

		dumpQuads.size should equal (13)

		val dumpCorrectQuads = CommonUtils.getQuads(List(
			// Schema
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/FirstName> <http://www.w3.org/2000/01/rdf-schema#label> \"first name\" <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/FirstName> <http://vocab.sindice.net/csv/columnPosition> \"0\"^^<http://www.w3.org/2001/XMLSchema#integer> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/LastName> <http://www.w3.org/2000/01/rdf-schema#label> \"last name\" <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/LastName> <http://vocab.sindice.net/csv/columnPosition> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://xmlns.com/foaf/0.1/homepage> <http://vocab.sindice.net/csv/columnPosition> \"2\"^^<http://www.w3.org/2001/XMLSchema#integer> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			// Data
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://vocab.sindice.net/csv/Row> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/FirstName> \"Charlie\"^^<http://www.w3.org/2001/XMLSchema#string> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/LastName> \"Brown\"^^<http://www.w3.org/2001/XMLSchema#string> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://xmlns.com/foaf/0.1/homepage> <http://www.peanuts.com/characters/charlie-brown> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			// Metadata
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/> <http://vocab.sindice.net/csv/row> <http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/row/0> <http://vocab.sindice.net/csv/rowPosition> \"0\" <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/> <http://vocab.sindice.net/csv/numberOfRows> \"1\"^^<http://www.w3.org/2001/XMLSchema#integer> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/> <http://vocab.sindice.net/csv/numberOfColumns> \"3\"^^<http://www.w3.org/2001/XMLSchema#integer> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> ."
			// any23.extraction.metadata.timesize = false
			//"<http://www4.wiwiss.fu-berlin.de/ldif/> <http://vocab.sindice.net/date> \"2013-02-26T12:24:29+01:00\" <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> ."
			//"<http://www4.wiwiss.fu-berlin.de/ldif/test_local_csv/> <http://vocab.sindice.net/size> \"15\"^^<http://www.w3.org/2001/XMLSchema#int> <http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> ."
		))
		OutputValidator.contains(dumpQuads, dumpCorrectQuads) should equal(true)

		val provenanceFile = dumpDir.listFiles().filter(_.getName.equals("test_local_csv.provenance.nq")).head
		val provenanceQuads = CommonUtils.getQuads(provenanceFile)

		provenanceQuads.size should equal (9)

		val provCorrectQuads = CommonUtils.getQuads(List(
			"_:test5Flocal5Fcsv <"+Consts.RDFTYPE_URI+"> <http://www4.wiwiss.fu-berlin.de/ldif/ImportJob> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"_:test5Flocal5Fcsv <"+Consts.importIdProp+"> \"test_local_csv\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			//"_:test5Flocal5Fcsv <"+Consts.lastUpdateProp+"> \"2013-02-26T13:07:13+01:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"_:test5Flocal5Fcsv <"+Consts.hasDatasourceProp+"> \"test\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"_:test5Flocal5Fcsv <"+Consts.hasImportTypeProp+"> \"csv\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"_:test5Flocal5Fcsv <"+Consts.hasOriginalLocationProp+"> \"scheduler/sources/source.csv\" <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"_:test5Flocal5Fcsv <"+Consts.numberOfQuadsProp+"> \"22\"^^<http://www.w3.org/2001/XMLSchema#nonNegativeInteger> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> <"+Consts.hasImportJobProp+"> _:test5Flocal5Fcsv <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> .",
			"<http://www4.wiwiss.fu-berlin.de/ldif/graph#test_local_csv> <"+Consts.RDFTYPE_URI+"> <"+Consts.importedGraphClass+"> <"+Consts.DEFAULT_PROVENANCE_GRAPH+"> ."
		))
		OutputValidator.contains(provenanceQuads, provCorrectQuads) should equal(true)
	}

  lazy val importJobRemote = {
    /* Disabled - remote test */
    val url = "https://raw.github.com/wbsg/ldif/master/ldif/ldif-singlemachine/src/test/resources/integration/sources/source.nq"
    QuadImportJob(url,"test_remote","never","test")
  }

  lazy val importJobLocalNq = {
    val url = "scheduler/sources/source.nq"
    QuadImportJob(url,"test_local","always","test","#.+")
  }

	lazy val importJobLocalCsv = {
		val url = "scheduler/sources/source.csv"
		CsvImportJob(url,"test_local_csv","always","test")
	}

	lazy val correctImportJobs = {
		List(importJobRemote, importJobLocalNq, importJobLocalCsv).sortBy(_.id.toString)
	}


}

