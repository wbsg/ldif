package test

import ldif.modules.r2r.hadoop.RunHadoopR2RJob

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/22/11
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */

object RunLDIFHadoopPipeline {
  def main(args: Array[String]) {
    RunHadoopEntityBuilder.main(args)
    RunHadoopR2RJob.main(args)
  }

}