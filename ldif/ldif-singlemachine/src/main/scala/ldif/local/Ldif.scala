package ldif.local

import config.SchedulerConfig
import java.io.File
import java.util.logging.Logger

object Ldif {

  private val log = Logger.getLogger(getClass.getName)

  def main(args : Array[String])
  {
    var debug = false
    if(args.length<1) {
      println("No configuration file given.")
      System.exit(1)
    }
    else if(args.length>=2 && args(0)=="--debug")
      debug = true

    val configFile = if(args.length == 0) {
      val configUrl = getClass.getClassLoader.getResource("ldif/local/neurowiki/scheduler-config.xml")
      new File(configUrl.toString.stripPrefix("file:"))
    } else
      new File(args(args.length-1))

    if(!configFile.exists)
      log.warning("Configuration file not found at "+ configFile.getCanonicalPath)
    else {
      // Setup Scheduler
      val config = SchedulerConfig.load(configFile)
      val scheduler = new Scheduler(config, debug)

      // Evaluate jobs at most once. Evaluate import first, then integrate.
      if (scheduler.runOnce) {
        scheduler.evaluateImportJobs
        Thread.sleep(1000)
        while (!scheduler.allJobsCompleted) {
            // wait for jobs to be completed
            Thread.sleep(1000)
        }
        scheduler.evaluateIntegrationJob(false)
        sys.exit(0)
      }
      else {
        log.info("Running LDIF as server")
        // Evaluate jobs every 10 sec, run as server
        while(true){
          scheduler.evaluateJobs
          Thread.sleep(10 * 1000)
        }
      }
    }
  }

}