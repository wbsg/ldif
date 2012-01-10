package ldif.hadoop

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/10/12
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */

object MainClassDispatcher {
  def main(args : Array[String])
  {
    if(args.length<1) {
      System.err.println("Error: No arguments given.")
      exitDispatcher
    }
    val app = args(0)
    app match {
      case "scheduler" => Ldif.main(args.slice(1, args.length))
      case "integrate" => HadoopIntegrationJob.main(args.slice(1, args.length))
      case _ => System.err.println("Error: command " + app + " invalid.")
        exitDispatcher
    }
  }

  private def exitDispatcher {
    System.err.println("Usage: hadoop jar ldif-hadoop-executor* scheduler <schedulerConfig>")
    System.err.println("OR     hadoop jar ldif-hadoop-executor* integrate <integrationJobConfig>")
    System.exit(1)
  }
}