/* 
 * Copyright 2011-2014 Universität Mannheim, MediaEvent Services GmbH & Co. KG
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