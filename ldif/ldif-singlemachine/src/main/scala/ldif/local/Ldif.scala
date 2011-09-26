package ldif.local

import config.SchedulerConfig
import java.io.File
import java.util.logging.Logger
import ldif.util.FatalErrorListener

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

      if (scheduler.runOnce) {
        scheduler.evaluateJobs
        while (!scheduler.allJobsCompleted) {
            // wait for jobs to be completed
            Thread.sleep(1000)
        }
        sys.exit(0)
      }
      else
        while(true){
          // evaluate jobs every 10 seconds
          runInBackground(scheduler.evaluateJobs)
          Thread.sleep(10 * 1000)
        }
    }
  }

  /* Evaluate an expression in the background  */
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