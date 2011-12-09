package ldif.hadoop.runtime

import ldif.util.Consts

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 5:23 PM
 * To change this template use File | Settings | File Templates.
 */

object HadoopUriMinting {
  def execute(datasetPath: String, outputPath: String, mintNamespace: String, mintPropertySet: Set[String]) {
    val hadoopTmpDir = "hadoop_tmp"+Consts.fileSeparator+"uriminting"
    HadoopGenerateMintedURIs.execute(datasetPath, hadoopTmpDir+"/sameAs", mintNamespace, mintPropertySet)
    RunHadoopUriRewriting.execute(datasetPath, hadoopTmpDir+"/sameAs", outputPath)
  }

  // For debugging
  def main(args: Array[String]) {
    val mintPropertySet = Set("http://www.w3.org/2000/01/rdf-schema#label", "http://mywiki/resource/property/Label", "http://mywiki/resource/property/AlternativeLabel") //TODO: add from config
    val mintNamespace = "http://minted/"
    execute("r2rOutput", "r2rTest", mintNamespace, mintPropertySet)
  }
}