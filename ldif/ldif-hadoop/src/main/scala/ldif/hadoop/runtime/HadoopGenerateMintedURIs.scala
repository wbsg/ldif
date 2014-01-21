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

package ldif.hadoop.runtime

import ldif.entity.NodeWritable
import ldif.hadoop.types.QuadWritable
import ldif.hadoop.io.{QuadSequenceFileOutput, QuadSequenceFileInput}
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import org.slf4j.LoggerFactory
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import ldif.hadoop.mappers.UriMintValuePickMapper
import ldif.hadoop.reducers.UriMintValuePickerReducer
import org.apache.hadoop.io.{Text, NullWritable}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */

class HadoopGenerateMintedURIs extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[HadoopGenerateMintedURIs])

    job.setJobName("GenerateMintedUri")

    job.setMapperClass(classOf[UriMintValuePickMapper])
    job.setReducerClass(classOf[UriMintValuePickerReducer])
    job.setMapOutputKeyClass(classOf[NodeWritable])
    job.setMapOutputValueClass(classOf[Text])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setInputFormat(classOf[QuadSequenceFileInput])
    job.setOutputFormat(classOf[QuadSequenceFileOutput])

    val in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object HadoopGenerateMintedURIs {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def execute(datasetInputPath: String, outputPath: String, mintNamespace: String, mintPropertySet: Set[String]): Int = {
    log.info("Starting To Generate Uri Minting Mappings...")
    val start = System.currentTimeMillis
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(outputPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    HadoopHelper.distributeSerializableObject(mintPropertySet, conf, "mintPropertySet")
    HadoopHelper.distributeSerializableObject(mintNamespace, conf, "mintNamespace")
    val res = ToolRunner.run(conf, new HadoopGenerateMintedURIs(), Array[String](datasetInputPath, outputPath))

    log.info("That's it. Generation of Uri Minting Mappings took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}