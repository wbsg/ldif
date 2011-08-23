package ldif.local

import java.util.logging.Logger
import java.util.Calendar
import scheduler.ImportJob
import util.Const
import java.io.File

class Scheduler (val config : LdifConfiguration) {
  val log = Logger.getLogger(getClass.getName)

  val importJobs = config.getImportJobs
  val sourceDir = config.getLocalSourceDir
  // val provenanceGraph = get provenance graph from properties

  /**
   * Updates local sources based on the defined import schedule.
   * This method is supposed to be invoked regularily by means such as a hourly cronjob.
   */
  def runUpdate {
    for (job <- importJobs.filter(checkUpdate(_))) {
       //runImport(job);
    }
  }

  def checkUpdate(job : ImportJob) : Boolean = {
      var lastUpdate : Calendar = null
      var nextUpdate : Calendar = null

      val changeFreqHours = Const.changeFreqToHours.get(job.changeFreq)

      /* Get last update run */
      val prevImportFile = new File(sourceDir + "/" + job.id +".nq")
      if (prevImportFile != null) {
        // lastUpdate = getLastUpdateFromFile(prevImportFile, provenanceGraph)
      }

      /* Figure out if update is required */
      if (changeFreqHours != None) {
        if (lastUpdate == null) {
          true
        } else {
          nextUpdate = lastUpdate
          nextUpdate.add(Calendar.HOUR, changeFreqHours.get)
          Calendar.getInstance.after(nextUpdate)
        }
      }
      else
        false
  }
}