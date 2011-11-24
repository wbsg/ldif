/* 
 * LDIF
 *
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


import org.apache.hadoop.conf.Configured._
import org.apache.hadoop.io.NullWritable
import ldif.hadoop.types.QuadWritable
import org.apache.hadoop.fs.{Path, FileSystem}
import ldif.hadoop.io.{EntityMultipleSequenceFileOutput, QuadTextFileOutput, EntitySequenceFileInput}
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import java.math.BigInteger
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/15/11
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */

class HadoopURITranslator extends Configured with Tool {//TODO finish
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[HadoopURITranslator])
    val fileSystem = FileSystem.get(conf)

//    job.setMapperClass(classOf[UriTransMapper])
    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setInputFormat(classOf[EntitySequenceFileInput])
    job.setOutputFormat(classOf[QuadTextFileOutput])

//    MultipleOutputs.addNamedOutput(job, "debug", classOf[QuadTextFileOutput], classOf[NullWritable], classOf[QuadWritable])

//    for(i <- 0 until  nrOfMappings) {
//      val in = new Path(args(0), EntityMultipleSequenceFileOutput.generateDirectoryName(i))
//      if(fileSystem.exists(in))
//        FileInputFormat.addInputPath(job, in)
//    }
    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object HadoopURITranslator {

  def main(args: Array[String]) {
    val res = execute(args(0), args(1), args(2))
    sys.exit(res)
  }

  def execute(sameAsLinksInputPath: String, quadsInputPath: String, outputPath: String): Int = {
    println("Starting Hadoop URI Translator...")

    FileUtils.deleteDirectory(new File(outputPath))
    val start = System.currentTimeMillis
    val conf = new Configuration
//    HadoopHelper.distributeSerializableObject(config?, conf, "uri_translator_config")
    val res = ToolRunner.run(conf, new HadoopURITranslator(), Array[String](sameAsLinksInputPath, quadsInputPath, outputPath))
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}