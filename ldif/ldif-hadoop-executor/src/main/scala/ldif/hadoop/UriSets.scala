/*
 * LDIF
 *
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

package ldif.hadoop

import runtime.{HadoopQuadToTextConverter, RunHadoopURIClustering, RunHadoopQuadConverter}
import org.apache.hadoop.fs.{Path, FileSystem}
import org.apache.hadoop.conf.Configuration

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 1/30/12
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */

object UriSets {
  def execute(args: Array[String]) {
    if(args.length < 2) {
      sys.error("Parameters: <sameAs input path> <output path>")
      sys.exit(1)
    }

    val tmp = "_tmp_" + System.currentTimeMillis() + "/"
    val quads = tmp + "quads"
    val clusteredUris = tmp + "clustered"
    val output = args(1)

    // remove existing output
    val conf = new Configuration
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(output)
    if (hdfs.exists(hdPath))
    hdfs.delete(hdPath, true)

    // Run parse job, clustering and then text conversion
    RunHadoopQuadConverter.execute(args(0), quads)
    RunHadoopURIClustering.runHadoopURIClustering(quads, clusteredUris)
    HadoopQuadToTextConverter.execute(clusteredUris, args(1))

    // Delete temp directory
    hdfs.delete(new Path(tmp), true)
  }
}