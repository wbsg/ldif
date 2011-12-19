/* 
 * Copyright 2011 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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