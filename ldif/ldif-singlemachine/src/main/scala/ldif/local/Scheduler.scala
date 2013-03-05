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

import ldif.config.SchedulerConfig
import org.slf4j.LoggerFactory
import scheduler.ImportJob
import java.util.{Date, Calendar}
import java.io._
import java.util.concurrent.ConcurrentHashMap
import org.apache.commons.io.FileUtils
import ldif.config.IntegrationConfig
import ldif.util.{TemporaryFileCreator, Consts, StopWatch, FatalErrorListener}
import util.ImportedDumpsUtils

case class Scheduler (config : SchedulerConfig, debug : Boolean = false) {
  private val log = LoggerFactory.getLogger(getClass.getName)

  // load jobs
  private val importJobs = loadImportJobs(config.importJobsFiles)
  private val integrationJob : IntegrationJob = loadIntegrationJob(config.integrationJob)

  // init status variables
  private var startup = true
  private var runningIntegrationJobs = false
  private val runningImportJobs = initRunningJobsMap

  private val importedDumps = ImportedDumpsUtils(config.dumpLocationDir)

  /* Evaluate and run the jobs */
  def run(stopExecution : Boolean = false, runStatusMonitor : Boolean) {
    if (runOnce) {
      // Evaluate jobs at most once. Evaluate import first, then integrate.
      evaluateImportJobs
      Thread.sleep(1000)
      while (!allJobsCompleted) {
        // wait for jobs to be completed
        Thread.sleep(1000)
      }
      evaluateIntegrationJob(false)
      if(stopExecution)    {
        if (runStatusMonitor) {
          println("Press any key to exit")
          System.in.read()
        }
        sys.exit(0)
      }
    }
    else {
      // Run as server, evaluate jobs every 10 sec
      log.info("Running LDIF as server")
      while(true){
        evaluateJobs
        Thread.sleep(10 * 1000)
      }
    }
  }

  /* Evaluate updates/integration */
  def evaluateJobs() {
    synchronized {
      evaluateIntegrationJob(true)
      evaluateImportJobs
      startup = false
    }
  }

  def evaluateIntegrationJob(inBackground : Boolean) {
    if (integrationJob != null && checkUpdate(integrationJob)) {
      runningIntegrationJobs = true
      if (inBackground)
        runInBackground{runIntegration()}
      else runIntegration()

    }
  }

  def evaluateImportJobs {
    for (job <- importJobs) {
      // check if this job is already running
      if (checkUpdate(job) && !runningImportJobs.get(job.id)) {
        runImport(job)
      }
      else job.reporter.setSkipped
    }
  }

  /* Evaluate if all jobs should run only once (at startup) or never */
  def runOnce : Boolean = {
    if (config.properties.getProperty("oneTimeExecution", "false") == "true") {
      log.info("One time execution enabled")
      return true
    }
    for (job <- importJobs)
      if (job.refreshSchedule != "onStartup" && job.refreshSchedule != "never")
        return false
    if (integrationJob != null && (integrationJob.config.runSchedule != "onStartup" && integrationJob.config.runSchedule != "never"))
      return false
    true
  }

  def allJobsCompleted = !runningIntegrationJobs && !runningImportJobs.containsValue(true)

  /* Execute the integration job */
  def runIntegration() {
      integrationJob.runIntegration
      log.info("Integration Job completed")
      runningIntegrationJobs = false
  }

  /* Execute an import job, update local source */
  def runImport(job : ImportJob) {
    runInBackground
    {
      runningImportJobs.replace(job.id, true)
      val stopWatch = new StopWatch
      log.info("Import Job "+ job.id +" started ("+job.getType+" / "+job.refreshSchedule+")")
      stopWatch.getTimeSpanInSeconds()

      val tmpDumpFile = getTmpDumpFile(job)
      val tmpProvenanceFile = getTmpProvenanceFile(job)

      // create local dump
      val success = try {
          job.load(new FileOutputStream(tmpDumpFile), importedDumps.getNumberOfQuads(job))
        } catch {
          case e: Exception => log.warn("There has been an unexpected error while processing job " + job +". Cause: " + e.getMessage)
          log.debug(e.getStackTraceString)
          false
        }


      if(success) {
        // create provenance metadata
        val provenanceGraph = config.properties.getProperty("provenanceGraphURI", Consts.DEFAULT_PROVENANCE_GRAPH)
        job.generateProvenanceInfo(new OutputStreamWriter(new FileOutputStream(tmpProvenanceFile)), provenanceGraph)

        log.info("Job " + job.id + " loaded in "+ stopWatch.getTimeSpanInSeconds + "s")

        var loop = true
        val changeFreqHours = Consts.changeFreqToHours.get(job.refreshSchedule).get
        var maxWaitingTime = changeFreqHours.toLong * 60 * 60
        val waitingInterval = 1

        while(loop) {
          // if the integration job is not running
          if (!runningIntegrationJobs) {
            // replace old dumps with the new ones
            moveFile(tmpDumpFile, getDumpFile(job))
            moveFile(tmpProvenanceFile, getProvenanceFile(job))
            log.info("Updated local dumps for job "+ job.id)

            runningImportJobs.replace(job.id, false)
            loop = false
          }
          else {
            // wait for integration job to be completed, but not more than the job refreshSchedule
            maxWaitingTime -= waitingInterval
            if (maxWaitingTime < 0) {
              // waited too long, dump is outdates
              log.info("The dump loaded for job "+ job.id +" expired, a new import is required. \n"+ tmpDumpFile.getCanonicalPath)
              if (!debug) {
                FileUtils.deleteQuietly(tmpDumpFile)
                FileUtils.deleteQuietly(tmpProvenanceFile)
              }
              runningImportJobs.replace(job.id, false)
              loop = false
            }
            Thread.sleep(waitingInterval * 1000)
          }
        }
      }
      else {
        job.reporter.setFailed()
        if (!debug) {
          FileUtils.deleteQuietly(tmpDumpFile)
          FileUtils.deleteQuietly(tmpProvenanceFile)
        }
        runningImportJobs.replace(job.id, false)
        log.warn("Job " + job.id + " has not been imported - see log for details")
      }
    }
  }

  private def initRunningJobsMap = {
    val map = new ConcurrentHashMap[String, Boolean](importJobs.size)
    for(job <- importJobs)
      map.putIfAbsent(job.id, false)
    map
  }

  /* Check if an update is required for the integration job */
  def checkUpdate(job : IntegrationJob = integrationJob) : Boolean = {
    checkUpdate(job.config.runSchedule, job.getLastUpdate)
  }

  /* Check if an update is required for the import job */
  def checkUpdate(job : ImportJob) : Boolean = {
    checkUpdate(job.refreshSchedule, importedDumps.getLastUpdate(job))
  }


  private def checkUpdate(schedule : String, lastUpdate : Calendar) : Boolean = {
    if (schedule == "onStartup") {
      if (startup)
        true
      else
        false
    }
    else if (schedule == "never") {
      false
    }
    else {
      val changeFreqHours = Consts.changeFreqToHours.get(schedule)
      // Get last update run
      val nextUpdate = Calendar.getInstance

      // Figure out if update is required
      if (changeFreqHours != None) {
        if (lastUpdate == null) {
          true
        } else {
          nextUpdate.setTimeInMillis(lastUpdate.getTimeInMillis)
          nextUpdate.add(Calendar.HOUR, changeFreqHours.get)
          Calendar.getInstance.after(nextUpdate)
        }
      }
      else
        false
    }
  }

  private def loadImportJobs(files : Traversable[File]) : Traversable[ImportJob] =
     files.map(ImportJob.load(_))

  // Build local files for the import job
  private def getDumpFile(job : ImportJob) = new File(config.dumpLocationDir, job.id +".nq")
  private def getTmpDumpFile(job : ImportJob) = TemporaryFileCreator.createTemporaryFile(job.id+"_"+Consts.simpleDateFormat.format(new Date()),".nq")
  private def getProvenanceFile(job : ImportJob) = new File(config.dumpLocationDir, job.id +".provenance.nq")
  private def getTmpProvenanceFile(job : ImportJob) = TemporaryFileCreator.createTemporaryFile(job.id+"_provenance_"+Consts.simpleDateFormat.format(new Date()),".nq")


  private def loadIntegrationJob(configFile : File) : IntegrationJob = {
    if(configFile != null)  {
      var integrationConfig = IntegrationConfig.load(configFile)
      // use dumpLocation as source directory for the integration job
      integrationConfig = integrationConfig.copy(sources = Traversable(config.dumpLocationDir))
      // if properties are not defined for the integration job, then use scheduler properties
      if (integrationConfig.properties.size == 0)
        integrationConfig = integrationConfig.copy(properties = config.properties)
      val integrationJob = IntegrationJob(integrationConfig, debug)
      log.info("Integration job loaded from "+ configFile.getCanonicalPath)
      integrationJob
    }
    else {
      log.warn("Integration job configuration file not found")
      null
    }
  }

  // Move source File to dest File
  private def moveFile(source : File, dest : File) {
    // delete dest (if exists)
    FileUtils.deleteQuietly(dest)
    if (!debug) {
      try {
        FileUtils.moveFile(source, dest)
      } catch {
        case ex:IOException => log.error("IO error occurs moving a file: \n" +source.getCanonicalPath+ " -> " +dest.getCanonicalPath)
      }
    }
    else {
      // if debugMode, keep tmp file
      try {
        FileUtils.copyFile(source, dest)
      } catch {
        case ex: IOException =>  {
          log.error("IO error occurs copying a file: \n"+source.getCanonicalPath+" to "+dest.getCanonicalPath)
        }
      }
    }
  }

  /**
   * Evaluates an expression in the background.
   */
  private def runInBackground(function : => Unit) {
    val thread = new Thread {
      private val listener: FatalErrorListener = FatalErrorListener

      override def run() {
        try {
          function
        } catch {
          case e: Exception => listener.reportError(e)
        }
      }
    }
    thread.start()
  }

  def getImportJobs = importJobs
  def getIntegrationJob = integrationJob

  def toXML : xml.Node = config.toXML
}

