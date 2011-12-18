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

import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.conf.{Configuration, Configured}
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.mapred._
import ldif.hadoop.io.QuadSequenceFileInput
import lib.IdentityMapper
import org.apache.hadoop.io.{Text, NullWritable}
import org.apache.hadoop.fs.{FileSystem, Path}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/8/11
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */

class HadoopQuadToTextConverter extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[HadoopQuadToTextConverter])

    job.setJobName("ConvertSeqToNq")

    job.setMapperClass(classOf[IdentityMapper[NullWritable, QuadWritable]])
    //job.setNumReduceTasks(0)

    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[Text])

    job.setInputFormat(classOf[QuadSequenceFileInput])
    job.setOutputFormat(classOf[TextOutputFormat[NullWritable, QuadWritable]])

    var in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object HadoopQuadToTextConverter {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def execute(inputPath: String, outputPath: String): Int = {
    log.info("Starting quad to N-Quads conversion...")
    val start = System.currentTimeMillis
    val conf = new Configuration

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(outputPath)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    val res = ToolRunner.run(conf, new HadoopQuadToTextConverter, Array[String](inputPath, outputPath))
    log.info("That's it. quad conversion took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }

  // For Debugging
  def main(args: Array[String]) {
    execute("r2rTest", "r2rTestText")
  }
}