package ldif.local

import config.{IntegrationConfig, SchedulerConfig}
import datasources.dump.QuadParser
import java.util.logging.Logger
import scheduler.ImportJob
import xml.XML
import java.util.{Date, Calendar}
import java.nio.channels.FileChannel
import java.io._
import java.util.concurrent.ConcurrentHashMap
import ldif.util.{StopWatch, FatalErrorListener, Consts}

class Scheduler (val config : SchedulerConfig, debug : Boolean = false) {
  private val log = Logger.getLogger(getClass.getName)

  // load jobs
  private val importJobs = loadImportJobs(config.importJobsDir)
  private val integrationJob : IntegrationJob = loadIntegrationJob(config.integrationJob)

  // init status variables
  private var startup = true
  private var runningIntegrationJobs = false
  private val runningImportJobs = initRunningJobsMap

  /* Evaluate updates/integration */
  def evaluateJobs {
    synchronized {
      if (integrationJob != null && checkUpdate(integrationJob)){
        runningIntegrationJobs = true
        runIntegration
      }
      for (job <- importJobs.filter(checkUpdate(_))) {
        // check if this job is already running
        if (!runningImportJobs.get(job.id)) {
          runImport(job)
        }
      }
      startup = false
    }
  }

  /* Evaluate if all jobs should run only once (at startup) or never */
  def runOnce : Boolean = {
    for (job <- importJobs)
      if (job.refreshSchedule != "onStartup" || job.refreshSchedule != "never")
        return false
    if (integrationJob != null && (integrationJob.config.runSchedule != "onStartup" || integrationJob.config.runSchedule != "never"))
      return false
    true
  }

  /* Execute the integration job */
  def runIntegration {
    runInBackground
    {
      integrationJob.runIntegration
      runningIntegrationJobs = false
    }
  }

  /* Execute an import job, update local source */
  def runImport(job : ImportJob) {
    runInBackground
    {
      val stopWatch = new StopWatch
      log.info("Running import job "+ job.id)
      stopWatch.getTimeSpanInSeconds

      val tmpDumpFile = getTmpDumpFile(job)
      val tmpProvenanceFile = getTmpProvenanceFile(job)

      // create local dump
      val success = job.load(new FileOutputStream(tmpDumpFile))

      if(success) {
        // create provenance metadata
        val provenanceGraph = config.properties.getProperty("provenanceGraphURI", Consts.DEFAULT_PROVENANCE_GRAPH)
        val updateTime = job.generateProvenanceInfo(new OutputStreamWriter(new FileOutputStream(tmpProvenanceFile)), provenanceGraph)

        log.info("Job " + job.id + " imported in "+ stopWatch.getTimeSpanInSeconds + " s")

        var loop = true
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
            //          TODO - dont wait integration for ever
            //          if (now > updateTime + job.refreshSchedule))  {
            //            // - slept too long :
            //            loop = false
            //          }
            //          else
            Thread.sleep(1000)
          }
        }
      }
      else
        log.warning("Job " + job.id + " has not been imported - see log f")
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
    checkUpdate(job.config.runSchedule, job.lastUpdate)
  }

  /* Check if an update is required for the import job */
  def checkUpdate(job : ImportJob) : Boolean = {
    checkUpdate(job.refreshSchedule, getLastUpdate(job))
  }


  private def checkUpdate(schedule : String, lastUpdate : Calendar) : Boolean = {
    if (startup && schedule == "onStartup") {
      true
    }
    else {
      val changeFreqHours = Consts.changeFreqToHours.get(schedule)

      // Get last update run
      var nextUpdate = lastUpdate

      // Figure out if update is required
      if (changeFreqHours != None) {
        if (lastUpdate == null) {
          true
        } else {
          nextUpdate.add(Calendar.HOUR, changeFreqHours.get)
          Calendar.getInstance.after(nextUpdate)
        }
      }
      else
        false
    }
  }

  /* Retrieve last update from provenance info */
  private def getLastUpdate(job : ImportJob) : Calendar = {
    val provenanceFile = getProvenanceFile(job)
    if (provenanceFile.exists) {
      val lines = scala.io.Source.fromFile(provenanceFile).getLines
      val parser = new QuadParser

      for (quad <- lines.toTraversable.map(parser.parseLine(_))){
        if (quad.predicate.equals(Consts.lastUpdateProp))  {
          val lastUpdateStr = quad.value.value
          if (lastUpdateStr.length != 25)
            log.info("Date not in expected xml datetime format")
          else {
            val sb = new StringBuilder(lastUpdateStr).deleteCharAt(22)
            val lastUpdateDate = Consts.xsdDateTimeFormat.parse(sb.toString)
            // Convert Date to a Calendar
            val lastUpdateCal = Calendar.getInstance
            lastUpdateCal.setTime(lastUpdateDate)
            return lastUpdateCal
          }
        }
      }
    }
    null
  }

  private def loadImportJobs(file : File) : Traversable[ImportJob] =  {
    if(file == null) {
      Traversable.empty[ImportJob]
    }
    else if(file.isFile) {
      Traversable(loadImportJob(file))
    }
    else {// file is a directory
      file.listFiles.toTraversable.map(loadImportJob(_))
    }
  }

  private def loadImportJob(file : File) = ImportJob.fromXML(XML.loadFile(file))

  // Build local files for the import job
  private def getDumpFile(job : ImportJob) = new File(config.dumpLocationDir, job.id +".nq")
  private def getTmpDumpFile(job : ImportJob) = File.createTempFile(job.id+"_"+Consts.simpleDateFormat.format(new Date()),".nq")
  private def getProvenanceFile(job : ImportJob) = new File(config.dumpLocationDir, job.id +".provenance.nq")
  private def getTmpProvenanceFile(job : ImportJob) = File.createTempFile(job.id+"_provenance_"+Consts.simpleDateFormat.format(new Date()),".nq")


  private def loadIntegrationJob(configFile : File) : IntegrationJob = {
    if(configFile != null)  {
      var integrationConfig = IntegrationConfig.load(configFile)
      // use dumpLocation as source directory for the integration job
      integrationConfig = integrationConfig.copy(sources = config.dumpLocationDir)
      val integrationJob = new IntegrationJob(integrationConfig, debug)
      log.info("Integration job loaded from "+ configFile.getCanonicalPath)
      integrationJob
    }
    else {
      log.warning("Configuration file not found")
      null
    }
  }

  // Move source File to dest File
  private def moveFile(source : File, dest : File) {
    if (!debug) {
      if (!source.renameTo(dest))
        log.severe("File was not successfully moved\n" +source.getCanonicalPath+ " -> " +dest.getCanonicalPath)
    }
    else {
      // if debug, keep tmp file
      var in, out : FileChannel = null
      try {
        in = new FileInputStream(source).getChannel
        out = new FileOutputStream(dest).getChannel
        val size = in.size
        val buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, size)
        out.write(buffer)
      } catch {
        case ex: IOException =>  {
          log.severe("IOException while copying file "+source.getCanonicalPath+" to "+dest.getCanonicalPath)
        }
      } finally {
        if (in != null) in.close
        if (out != null) out.close
      }
    }
  }

  /**
   * Evaluates an expression in the background.
   */
  private def runInBackground(function : => Unit) {
    val thread = new Thread {
      private val listener: FatalErrorListener = FatalErrorListener

      override def run {
        try {
          function
        } catch {
          case e: Exception => listener.reportError(e)
        }
      }
    }
    thread.start
  }
}

object Scheduler
{
  def main(args : Array[String])
  {
    val configFile = if(args.length == 0) {
      val configUrl = getClass.getClassLoader.getResource("ldif/local/neurowiki/scheduler-config.xml")
      new File(configUrl.toString.stripPrefix("file:"))
    } else
      new File(args(args.length-1))

    val scheduler = new Scheduler(SchedulerConfig.load(configFile))

    while(true){
      scheduler.evaluateJobs
      Thread.sleep(10 * 1000)
    }
  }
}

