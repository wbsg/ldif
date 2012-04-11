/*
 * LDIF
 *
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

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scheduler._
import ldif.config.SchedulerConfig
import ldif.util.{OutputValidator, CommonUtils}

@RunWith(classOf[JUnitRunner])
class SchedulerTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.loadFile("scheduler/scheduler-config.xml")
  val scheduler = Scheduler(SchedulerConfig.load(configFile))

  it should "schedule a job correctly" in {
    val importJobs = scheduler.getImportJobs
    if(importJobs.head.id=="test.local")
      scheduler.checkUpdate(scheduler.getImportJobs.head) should equal (true)
    else
      scheduler.checkUpdate(scheduler.getImportJobs.tail.head) should equal (true)
  }

  it should "parse a job configuration correctly" in {
    val importJobs = scheduler.getImportJobs
    if(importJobs.head.id=="test.local") {
      importJobs.head should equal (importJobLocal)
      importJobs.last should equal (importJobRemote)
    } else {
      importJobs.head should equal (importJobRemote)
      importJobs.last should equal (importJobLocal)
    }

  }

  it should "load a dump and provenance correctly" in {
    // run local import
    scheduler.evaluateImportJobs
    // wait for the import to be completed
    Thread.sleep(1000)

    val dumpDir = new File(scheduler.config.dumpLocationDir)
    val dumpFile = dumpDir.listFiles().filter(_.getName.equals("test.local.nq")).head
    val dumpQuads = CommonUtils.getQuads(dumpFile)

    dumpQuads.size should equal (8)

    val dumpCorrectQuads = CommonUtils.getQuads(List(
      "<http://source/graph1> <http://ldif/provProp>  \"_\" <http://ldif/provGraph> . ",
      "<http://source/uriA> <http://www.w3.org/2002/07/owl#sameAs> <http://source/uriC> <http://source/graph1> . ",
      "<http://source/uriA> <http://source/mintProp> \"mint\" <http://source/graph2> . ",
      "<http://source/uriA> <http://source/mapProp> \"map\" <http://source/graph3> . ",
      "<http://source/uriB> <http://source/mapProp> \"map\" <http://source/graph4> . ",
      "<http://source/uriD> <http://source/mintProp> \"mintNotMapped\" <http://source/graph5> . ",
      "<http://source/uriA> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://source/class> <http://source/graph6>. ",
      "<http://source/uriB> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://source/class> <http://source/graph7> . "
    ))
    OutputValidator.contains(dumpQuads, dumpCorrectQuads) should equal(true)

    val provenanceFile = dumpDir.listFiles().filter(_.getName.equals("test.local.provenance.nq")).head
    val provenanceQuads = CommonUtils.getQuads(provenanceFile)

    provenanceQuads.size should equal (23)

    val provCorrectQuads = CommonUtils.getQuads(List(
      "_:test2Elocal <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/ldif/ImportJob> <http://www4.wiwiss.fu-berlin.de/ldif/provenance> . ",
      "_:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/importId> \"test.local\" <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "_:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/hasDatasource> \"test\" <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "_:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/hasImportType> \"quad\" <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "_:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/numberOfQuads> \"31\"^^<http://www.w3.org/2001/XMLSchema#double> <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "_:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/hasOriginalLocation> \"ldif-singlemachine/target/test-classes/scheduler/sources/source.nq\" <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "<http://source/graph3> <http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob> _:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "<http://source/graph3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph> <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "<http://source/graph1> <http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob> _:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "<http://source/graph1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www4.wiwiss.fu-berlin.de/ldif/ImportedGraph> <http://www4.wiwiss.fu-berlin.de/ldif/provenance> .",
      "<http://source/graph6> <http://www4.wiwiss.fu-berlin.de/ldif/hasImportJob> _:test2Elocal <http://www4.wiwiss.fu-berlin.de/ldif/provenance> ."
    ))
    OutputValidator.contains(provenanceQuads, provCorrectQuads) should equal(true)
  }

  lazy val importJobRemote = {
    /* Disabled - remote test */
    val url = "http://www.assembla.com/code/ldif/git/node/blob/ldif/ldif-singlemachine/src/test/resources/integration/sources/source.nq"
    QuadImportJob(url,"test.remote","never","test")
  }

  lazy val importJobLocal = {
    val url = "ldif-singlemachine/target/test-classes/scheduler/sources/source.nq"
    QuadImportJob(url,"test.local","always","test","#.+")
  }

}

