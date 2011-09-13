package ldif.local

import datasources.dump.QuadParser
import java.util.logging.Logger
import scheduler.ImportJob
import ldif.config.SchedulerConfig
import xml.XML
import java.util.{Date, Calendar}
import ldif.util.Consts
import java.nio.channels.FileChannel
import java.io._

class Scheduler (val config : SchedulerConfig, debug : Boolean = false) {
  val log = Logger.getLogger(getClass.getName)

  val lastUpdateProperty = config.getLastUpdateProperty
  val importJobs = loadImportJobs(config.importJobsDir)
  val localSourceDir = config.dumpLocationDir
  val provenanceGraph = config.properties.getProperty("provenanceGraphURI", Consts.DEFAULT_PROVENANCE_GRAPH)
  var startup = true

  /**
   * Updates local sources based on the defined import schedule.
   * This method is supposed to be invoked regularily by means such as a hourly cronjob.
   */
  def runUpdate() {
    synchronized {
      log.info("Running update")
      for (job <- importJobs.filter(checkUpdate(_))) {
        val tmpDumpFile = getTmpDumpFile(job)
        val tmpProvenanceFile = getTmpProvenanceFile(job)

        job.load(new FileOutputStream(tmpDumpFile))
        // append provenance quads
        job.generateProvenanceInfo(new OutputStreamWriter(new FileOutputStream(tmpProvenanceFile)), provenanceGraph)

        // replace old dumps with the new ones
        // TODO check that no integration job is running before moving files
        copyFile(tmpDumpFile, getDumpFile(job))
        copyFile(tmpProvenanceFile, getProvenanceFile(job))
      }
      startup = false
    }
  }

  /* Check if an update is required for the job */
  def checkUpdate(job : ImportJob) : Boolean = {
    if (startup && job.refreshSchedule == "onStartup") {
        true
    }
    else {
      val changeFreqHours = Consts.changeFreqToHours.get(job.refreshSchedule)

      // Get last update run
      var lastUpdate, nextUpdate = getLastUpdate(job)

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
        if (quad.predicate.equals(lastUpdateProperty))  {
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

  private def loadImportJobs(file : File) : Traversable[ImportJob] =
  {
    if(file.isFile)
    {
      Traversable(loadImportJob(file))
    }
    else if(file.isDirectory)
    {
      file.listFiles.toTraversable.map(loadImportJob(_))
    }
    else {
      Traversable.empty[ImportJob]
    }

  }

  private def loadImportJob(file : File) = ImportJob.fromXML(XML.loadFile(file))

  // Build local files for the import job
  private def getDumpFile(job : ImportJob) = new File(localSourceDir, job.id +".nq")
  private def getTmpDumpFile(job : ImportJob) = File.createTempFile(job.id+"_"+Consts.simpleDateFormat.format(new Date()),".nq")
  private def getProvenanceFile(job : ImportJob) = new File(localSourceDir, job.id +".provenance.nq")
  private def getTmpProvenanceFile(job : ImportJob) = File.createTempFile(job.id+"_provenance_"+Consts.simpleDateFormat.format(new Date()),".nq")

  // Copy the source File in the dest File
  private def copyFile(source : File, dest : File) {
    var in, out : FileChannel = null
    try {
      in = new FileInputStream(source).getChannel
      out = new FileOutputStream(dest).getChannel
      val size = in.size
      val buffer = in.map(FileChannel.MapMode.READ_ONLY, 0, size)
      out.write(buffer)
    } catch {
      case ex: IOException =>  {
        log.severe("IOException while moving file "+source.getCanonicalPath+" to "+dest.getCanonicalPath)
        throw ex
      }
    } finally {
      if (in != null) in.close
      if (out != null) out.close
    }
  }
}

object Scheduler
{
  def main(args : Array[String])
  {
//    var debug = false
//    if(args.length<1) {
//      println("No configuration file given.")
//      System.exit(1)
//    }
//    else if(args.length>=2 && args(0)=="--debug")
//      debug = true

    val configUrl = getClass.getClassLoader.getResource("ldif/local/neurowiki/scheduler-config.xml")
    val configFile = new File(configUrl.toString.stripPrefix("file:"))
//    val configFile = new File(args(args.length-1))
    val scheduler = new Scheduler(SchedulerConfig.load(configFile))

    // Run update every one hour
    while(true){
      runInBackground(scheduler.runUpdate)
      Thread.sleep(60 * 60 * 1000)
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

}
