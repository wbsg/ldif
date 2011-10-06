import org.apache.hadoop.conf._
import org.apache.hadoop.util._

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 10/6/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */


class RunHadoop extends Configured with Tool {
  def run(args: Array[String]): Int = {
    0
  }
}

object RunHadoop {
  def main(args: Array[String]) {
    println("Starting...")
    val start = System.currentTimeMillis
//    val res = ToolRunner.run(new RunHadoop(), args)
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
  }
}