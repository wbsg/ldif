/* 
 * Copyright 2011-2012 Freie Universität Berlin, MediaEvent Services GmbH & Co. KG
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

import org.apache.hadoop.conf.Configured._
import ldif.hadoop.reducers.UriRewritingReducer
import ldif.entity.NodeWritable
import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.io.NullWritable
import ldif.hadoop.io.{QuadSequenceFileOutput, QuadSequenceFileInput}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import ldif.hadoop.mappers.{ConvertTextToQuadsMapper, UriRewritingMapper}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 12/5/11
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Converts an N-Triples or N-Quads file into a Quad Sequence file
 * (for testing purposes)
 */
class RunHadoopQuadConverter extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopUriRewriting])

    job.setMapperClass(classOf[ConvertTextToQuadsMapper])
    job.setNumReduceTasks(0)

    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setOutputFormat(classOf[QuadSequenceFileOutput])

//    MultipleOutputs.addNamedOutput(job, "debug", classOf[QuadTextFileOutput], classOf[NullWritable], classOf[QuadWritable])

    var in = new Path(args(0))
    FileInputFormat.addInputPath(job, in)

    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopQuadConverter {

  def execute(datasetInputPath: String, outputPath: String): Int = {//TODO: Add using temp directory for intermediary results (also for other jobs)
    println("Starting quad conversion...")
    val start = System.currentTimeMillis
    FileUtils.deleteDirectory(new File(outputPath))
    val conf = new Configuration
    val res = ToolRunner.run(conf, new RunHadoopQuadConverter(), Array[String](datasetInputPath, outputPath))
    println("That's it. quad conversion took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}