package ldif.local

import datasources.dump.QuadParser
import java.util.logging.Logger
import java.util.Calendar
import scheduler.ImportJob
import util.Const
import ldif.config.SchedulerConfig
import java.io.{FileWriter, File}
import javax.xml.bind.DatatypeConverter
import xml.XML

class Scheduler (val config : SchedulerConfig) {
  val log = Logger.getLogger(getClass.getName)

  val lastUpdateProperty = config.getLastUpdateProperty
  val importJobs = loadImportJobs(config.importJobsDir)
  val localSourceDir = config.properties.getProperty("dumpLocation", Const.DEFAULT_DUMP_LOCATION)
  val provenanceGraph = config.properties.getProperty("provenanceGraphURI", Const.DEFAULT_PROVENANCE_GRAPH)

  /**
   * Updates local sources based on the defined import schedule.
   * This method is supposed to be invoked regularily by means such as a hourly cronjob.
   */
  def runUpdate {
    for (job <- importJobs.filter(checkUpdate(_))) {
      job.load(new FileWriter(getTmpDumpPath(job)))

      // append provenance quads
      job.generateProvenanceInfo(new FileWriter(getTmpDumpProvenancePath(job)))

      // replace old dumps with the tmp ones
      //TODO check if an integration job is running

    }
  }

  // Check if an update is required for the job
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

  // Build dump local path for the import job
  private def getDumpPath(job : ImportJob) = new File(localSourceDir + "/" + job.id +".nq")
  private def getTmpDumpPath(job : ImportJob) = new File(localSourceDir + "/tmp/" + job.id +".nq")

  // Build dump local path for the import job
  private def getDumpProvenancePath(job : ImportJob) = new File(localSourceDir + "/" + job.id +".provenance.nq")
  private def getTmpDumpProvenancePath(job : ImportJob) = new File(localSourceDir + "/tmp/" + job.id +".provenance.nq")

  // Retrieve last update from provenance info
  private def getLastUpdate(job : ImportJob) : Calendar = {
    val dumpProvenance = getDumpProvenancePath(job)
    if (dumpProvenance.exists) {
      val lines = scala.io.Source.fromFile(dumpProvenance).getLines
      val parser = new QuadParser

      for (quad <- lines.toTraversable.map(parser.parseLine(_))){
        if (quad.predicate.equals(lastUpdateProperty))
           return DatatypeConverter.parseDateTime(quad.value.toString)
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

}