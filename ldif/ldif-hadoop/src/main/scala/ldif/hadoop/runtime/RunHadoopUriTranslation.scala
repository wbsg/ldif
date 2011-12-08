package ldif.hadoop.runtime

import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/5/11
 * Time: 4:50 PM
 * To change this template use File | Settings | File Templates.
 */

object RunHadoopUriTranslation {
  def execute(datasetPath: String, sameasPath: String, outputPath: String) {
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"uritranslation"
    RunHadoopURIClustering.runHadoopURIClustering(sameasPath,hadoopTmpDir+"/uriclustering")
    RunHadoopUriRewriting.execute(datasetPath, hadoopTmpDir+"/uriclustering", outputPath)
  }

  def main(args: Array[String]) = {
    if(args.length>=3)
      execute(args(0), args(1), args(2))
    else
      println("Parameters needed: <datasetPath> <sameAsLinksPath> <outputPath>")
  }
}