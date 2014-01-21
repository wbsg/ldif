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

package ldif.local

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scheduler._
import ldif.config.SchedulerConfig
import ldif.util.{Identifier, OutputValidator, CommonUtils}

@RunWith(classOf[JUnitRunner])
class SchedulerTest extends FlatSpec with ShouldMatchers {

  val configFile = CommonUtils.getFileFromPath("scheduler/scheduler-config.xml")
  val scheduler = Scheduler(SchedulerConfig.load(configFile))
  val dumpDir = scheduler.config.dumpLocationDir
  val correctDumpDir = "scheduler/correct"

  it should "schedule a job correctly" in {
    val importJobs = scheduler.getImportJobs
    scheduler.checkUpdate(scheduler.getImportJobs.tail.head) should equal (true)
  }

  it should "parse a job configuration correctly" in {
    val importJobs = scheduler.getImportJobs.toList.sortBy(_.id.toString)
    // Compare import jobs lists size
    importJobs.size should equal (correctImportJobs.size)
    // Compare import jobs lists content
    importJobs.corresponds(correctImportJobs) {_ == _} should equal (true)
  }

  // Run local import jobs
  scheduler.evaluateImportJobs
  while (!scheduler.allJobsCompleted) {
    // wait for jobs to be completed
    Thread.sleep(1000)
  }

  it should "load a N-Quads dump and provenance correctly" in {
    val (dumpQuads, dumpQuadsCorrect, provenanceQuads, provenanceQuadsCorrect) = getQuads("test_nquad")
    dumpQuads.size should equal (8)
    OutputValidator.contains(dumpQuads, dumpQuadsCorrect) should equal(true)
    provenanceQuads.size should equal (23)
    OutputValidator.contains(provenanceQuads, provenanceQuadsCorrect) should equal(true)
  }

  it should "load a CSV dump and provenance correctly" in {
    val (dumpQuads, dumpQuadsCorrect, provenanceQuads, provenanceQuadsCorrect) = getQuads("test_csv")
    dumpQuads.size should equal (13)
    OutputValidator.contains(dumpQuads, dumpQuadsCorrect) should equal(true)
    provenanceQuads.size should equal (9)
    OutputValidator.contains(provenanceQuads, provenanceQuadsCorrect) should equal(true)
  }

  it should "load a RDFa data and provenance correctly" in {
    val (dumpQuads, dumpQuadsCorrect, provenanceQuads, provenanceQuadsCorrect) = getQuads("test_rdfa")
    dumpQuads.size should equal (5)
    OutputValidator.contains(dumpQuads, dumpQuadsCorrect) should equal(true)
    provenanceQuads.size should equal (9)
    OutputValidator.contains(provenanceQuads, provenanceQuadsCorrect) should equal(true)
  }

  it should "load a XLSX data and provenance correctly" in {
    val (dumpQuads, dumpQuadsCorrect, provenanceQuads, provenanceQuadsCorrect) = getQuads("test_xlsx")
    dumpQuads.size should equal (88)
    OutputValidator.contains(dumpQuads, dumpQuadsCorrect) should equal(true)
    provenanceQuads.size should equal (9)
    OutputValidator.contains(provenanceQuads, provenanceQuadsCorrect) should equal(true)
  }

  // Helpers

  def getQuads(jobId : Identifier) = {
    (CommonUtils.getQuadsFromPath(dumpDir + "/" + jobId + ".nq"),
      CommonUtils.getQuadsFromPath(correctDumpDir + "/" + jobId + ".nq"),
      CommonUtils.getQuadsFromPath(dumpDir + "/" + jobId + ".provenance.nq"),
      CommonUtils.getQuadsFromPath(correctDumpDir + "/" + jobId + ".provenance.nq")
      )
  }

  lazy val importJobRemote = {
    /* Disabled - remote test */
    val url = "https://raw.github.com/wbsg/ldif/master/ldif/ldif-singlemachine/src/test/resources/integration/sources/source.nq"
    QuadImportJob(url,"test_nquad_remote","never","test")
  }

  lazy val importJobLocalNq = {
    val url = "scheduler/sources/source.nq"
    QuadImportJob(url,"test_nquad","always","test","#.+")
  }

  lazy val importJobLocalCsv = {
    val url = "scheduler/sources/source.csv"
    CsvImportJob(url,"test_csv","always","test")
  }

  lazy val importJobLocalRdfa = {
    val url = "scheduler/sources/source-rdfa.html"
    RDFaImportJob(url,"test_rdfa","always","test")
  }

  lazy val importJobLocalXlsx = {
    val url = "scheduler/sources/source.xlsx"
    XlsxImportJob(url,"test_xlsx","always","test")
  }

  lazy val correctImportJobs = {
    List(importJobRemote, importJobLocalNq, importJobLocalCsv, importJobLocalRdfa, importJobLocalXlsx).sortBy(_.id.toString)
  }

}

