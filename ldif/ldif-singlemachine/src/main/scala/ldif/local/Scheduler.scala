package ldif.local

import datasources.dump.QuadParser
import java.util.logging.Logger
import scheduler.ImportJob
import util.Const
import ldif.config.SchedulerConfig
import javax.xml.bind.DatatypeConverter
import xml.XML
import java.io.{FileOutputStream, OutputStreamWriter, File}
import java.util.{Date, Calendar}

class Scheduler (val config : SchedulerConfig) {
  val log = Logger.getLogger(getClass.getName)

  val lastUpdateProperty = config.getLastUpdateProperty
  val importJobs = loadImportJobs(config.importJobsDir)
  val localSourceDir = config.dumpLocationDir
  val provenanceGraph = config.properties.getProperty("provenanceGraphURI", Const.DEFAULT_PROVENANCE_GRAPH)

  /**
   * Updates local sources based on the defined import schedule.
   * This method is supposed to be invoked regularily by means such as a hourly cronjob.
   */
  def runUpdate {
    for (job <- importJobs.filter(checkUpdate(_))) {
      job.load(new OutputStreamWriter(new FileOutputStream(getDumpPath(job))))

      // append provenance quads
      job.generateProvenanceInfo(new OutputStreamWriter(new FileOutputStream(getDumpProvenancePath(job))), provenanceGraph)

      // replace old dumps with the tmp ones
      // TODO check that no integration job is running and move tmp file

    }
  }

  /* Check if an update is required for the job */
  def checkUpdate(job : ImportJob) : Boolean = {
    val changeFreqHours = Const.changeFreqToHours.get(job.refreshSchedule)

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

  /* Retrieve last update from provenance info */
  private def getLastUpdate(job : ImportJob) : Calendar = {
    val dumpProvenance = getDumpProvenancePath(job)
    if (dumpProvenance.exists) {
      val lines = scala.io.Source.fromFile(dumpProvenance).getLines
      val parser = new QuadParser

      for (quad <- lines.toTraversable.map(parser.parseLine(_))){
        if (quad.predicate.equals(lastUpdateProperty))  {
          val lastUpdateStr = quad.value.value
          if (lastUpdateStr.length != 25)
            log.info("Date not in expected xml datetime format")
          else {
            val sb = new StringBuilder(lastUpdateStr).deleteCharAt(22)
            val lastUpdateDate = Const.xsdDateTimeFormat.parse(sb.toString)
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

  // Build dump local path for the import job
  private def getDumpPath(job : ImportJob) = new File(localSourceDir, job.id +".nq")
  private def getTmpDumpPath(job : ImportJob) = new File(localSourceDir, job.id +"_"+ Const.simpleDateFormat.format(new Date()) +".nq")

  // Build dump local path for the import job
  private def getDumpProvenancePath(job : ImportJob) = new File(localSourceDir, job.id +".provenance.nq")
  private def getTmpDumpProvenancePath(job : ImportJob) = new File(localSourceDir, job.id +"_"+ Const.simpleDateFormat.format(new Date()) +".provenance.nq")

}