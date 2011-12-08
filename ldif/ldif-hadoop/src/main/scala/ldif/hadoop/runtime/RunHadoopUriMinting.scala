package ldif.hadoop.runtime

import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */

object RunHadoopUriMinting {
  def execute(datasetPath: String, outputPath: String) {
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"uriminting"
    RunHadoopGenerateMintedURIs.execute(datasetPath, hadoopTmpDir+"/sameAs")
    RunHadoopUriRewriting.execute(datasetPath, hadoopTmpDir+"/sameAs", outputPath)
  }
}