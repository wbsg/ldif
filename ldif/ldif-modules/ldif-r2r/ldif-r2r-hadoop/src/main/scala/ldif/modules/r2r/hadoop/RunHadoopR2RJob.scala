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

package ldif.modules.r2r.hadoop

import org.apache.hadoop.mapred.lib.{MultipleOutputs, NullOutputFormat}
import org.apache.hadoop.mapred.{JobClient, FileOutputFormat, FileInputFormat, JobConf}
import java.math.BigInteger
import de.fuberlin.wiwiss.r2r.{JenaModelSource, Repository, EnumeratingURIGenerator, FileOrURISource}
import de.fuberlin.wiwiss.r2r.LDIFMapping
import ldif.entity.{EntityDescriptionMetaDataExtractor, EntityDescription}
import org.apache.commons.io.FileUtils
import java.io.File
import org.apache.hadoop.conf.{Configuration, Configured}
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.util.{ToolRunner, Tool}
import org.apache.hadoop.io.{NullWritable, IntWritable}
import ldif.hadoop.types.{QuadWritable, ValuePathWritable}
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.collection.JavaConversions._
import ldif.hadoop.io.{QuadTextFileOutput, EntityMultipleSequenceFileOutput, EntitySequenceFileInput, QuadSequenceFileOutput}

/**
 * Created by IntelliJ IDEA.
 * User: andreas
 * Date: 11/17/11
 * Time: 5:39 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class represents a R2R Hadoop job that maps entities created by the Hadoop entity builder.
 * The output is written to one directory as quads.
 */
class RunHadoopR2RJob extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[RunHadoopR2RJob])
    val fileSystem = FileSystem.get(conf)
    val nrOfMappings = args(2).toInt

    job.setNumReduceTasks(0)
    job.setMapperClass(classOf[R2RMapper])
    job.setMapOutputKeyClass(classOf[NullWritable])
    job.setMapOutputValueClass(classOf[QuadWritable])
    job.setOutputKeyClass(classOf[NullWritable])
    job.setOutputValueClass(classOf[QuadWritable])

    job.setInputFormat(classOf[EntitySequenceFileInput])
    job.setOutputFormat(classOf[QuadTextFileOutput])

//    MultipleOutputs.addNamedOutput(job, "debug", classOf[QuadTextFileOutput], classOf[NullWritable], classOf[QuadWritable])

    for(i <- 0 until  nrOfMappings) {
      val in = new Path(args(0), EntityMultipleSequenceFileOutput.generateDirectoryName(i))
      if(fileSystem.exists(in))
        FileInputFormat.addInputPath(job, in)
    }
    val out = new Path(args(1))
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object RunHadoopR2RJob {

  def runHadoopR2RJob(inputPath: String, outputPath: String, mappings: IndexedSeq[LDIFMapping]): Int = {
    val res = execute(outputPath+"_4", outputPath+"_r2r", mappings)
    res
  }

  def execute(inputPath: String, outputPath: String, mappings: IndexedSeq[LDIFMapping]): Int = {
    println("Starting R2R Job")

    FileUtils.deleteDirectory(new File(outputPath))
    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(mappings, conf, "mappings")
    val res = ToolRunner.run(conf, new RunHadoopR2RJob(), Array[String](inputPath, outputPath, mappings.length.toString))
    println("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}