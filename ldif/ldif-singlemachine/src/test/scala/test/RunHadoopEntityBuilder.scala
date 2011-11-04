package test

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/4/11
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */

object RunHadoopEntityBuilder {
  def main(args: Array[String]) {
    if(args.length > 1) {
      RunPhase2.runPhase((args(0)::List(args(1)+"_2")).toArray)
      RunPhase3.runPhase((args(1)+"_2"::List(args(1)+"_3")).toArray)
      RunPhase4.runPhase((args(1)+"_3"::List(args(1)+"_4")).toArray)
    } else {
      sys.error("Not enough arguments! Arguments: <in> <out>")
    }
  }
}