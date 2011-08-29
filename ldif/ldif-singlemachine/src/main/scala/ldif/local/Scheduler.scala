package ldif.local

import java.util.logging.Logger
import java.util.Calendar
import scheduler.ImportJob
import util.Const
import java.io.{FileWriter, File}

class Scheduler (val config : LdifConfiguration) {
  val log = Logger.getLogger(getClass.getName)

  val importJobs = config.getImportJobs
  val localSourceDir = config.getLocalSourceDir
  // val provenanceGraph = get provenance graph name

  /**
   * Updates local sources based on the defined import schedule.
   * This method is supposed to be invoked regularily by means such as a hourly cronjob.
   */
  def runUpdate {
    for (job <- importJobs.filter(checkUpdate(_))) {
      job.load(new FileWriter(getLocalPath(job)))

      // append provenance quads
      //TODO
    }
  }

  // Check if an update is required for the job
  def checkUpdate(job : ImportJob) : Boolean = {
    val changeFreqHours = Const.changeFreqToHours.get(job.changeFreq)

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
  private def getLocalPath(job : ImportJob) = new File(localSourceDir + "/" + job.id +".nq")

  // Retrieve last update from provenance info
  private def getLastUpdate(job : ImportJob) : Calendar = {
    val localDump = getLocalPath(job)
    if (localDump.exists) {
      //TODO get last update from provenance graph
      Calendar.getInstance
    }
    else null
  }

}