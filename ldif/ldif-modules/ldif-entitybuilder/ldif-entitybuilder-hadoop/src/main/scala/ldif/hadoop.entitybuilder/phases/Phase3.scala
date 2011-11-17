/* 
 * Copyright 2011 Freie Universität Berlin and MediaEvent Services GmbH & Co. K 
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

package ldif.hadoop.entitybuilder.phases

import ldif.hadoop.entitybuilder.mappers._
import ldif.hadoop.entitybuilder.reducers._
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapred._
import lib.{MultipleOutputs, NullOutputFormat}
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.commons.io.FileUtils
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types._
import java.io.File
import ldif.hadoop.entitybuilder.io._
import ldif.hadoop.utils.HadoopHelper
import ldif.util.Consts
import ldif.entity.{EntityDescriptionMetadata, EntityDescription, EntityDescriptionMetaDataExtractor}
import scala.Array
import java.util.logging.Logger

/**
 *  Hadoop EntityBuilder - Phase 3
 *  Joining value paths
 **/

class Phase3 extends Configured with Tool {

  def run(args: Array[String]): Int = {

    val conf = getConf
    val job = new JobConf(conf, classOf[Phase3])

    val maxPhase = args(0).toInt
    val phase = args(1).toInt

    job.setMapperClass(classOf[ValuePathJoinMapper])
    job.setReducerClass(classOf[ValuePathJoinReducer])
    job.setNumReduceTasks(1)

    job.setMapOutputKeyClass(classOf[PathJoinValueWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])

    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])

    job.setInputFormat(classOf[ValuePathSequenceFileInput])
    job.setOutputFormat(classOf[NullOutputFormat[IntWritable, ValuePathWritable]])

    MultipleOutputs.addNamedOutput(job, "seq", classOf[ValuePathMultipleSequenceFileOutput], classOf[IntWritable], classOf[ValuePathWritable])
    // For debugging
    MultipleOutputs.addNamedOutput(job, "text", classOf[ValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    /* Add the JoinPaths for this phase (which were put into phase: (phase+1))
     * Don't do this if there is no join phase (maxPhase==0)
      */
    if(maxPhase>0) {
      var in = new Path(args(2), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase+1))
      FileInputFormat.addInputPath(job, in)
    }

    if(phase==0) {
      // Add the initial EntityPaths for first phase
      var in = new Path(args(2), JoinValuePathMultipleSequenceFileOutput.generateDirectoryName(phase))
      FileInputFormat.addInputPath(job, in)
    } else {
      // Add the constructed EntityPaths from the previous phase
      var in = new Path(args(3) + Consts.fileSeparator  + (phase-1), ValuePathMultipleSequenceFileOutput.generateDirectoryNameForValuePathsInConstruction(phase-1))
      FileInputFormat.addInputPath(job, in)
    }


    val out = new Path(args(3) + Consts.fileSeparator  + phase)
    FileOutputFormat.setOutputPath(job, out)

    JobClient.runJob(job)

    return 0
  }
}

object Phase3 {
  private val log = Logger.getLogger(getClass.getName)

  def runPhase(in : String, out : String, entityDescriptions : Seq[EntityDescription]) : Int = {
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)
    runPhase(in,out,edmd)
  }

  def runPhase(in : String, out : String, edmd : EntityDescriptionMetadata) : Int = {
    log.info("Starting phase 3 of the EntityBuilder: Joining value paths")

    FileUtils.deleteDirectory(new File(out))

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    var res = 0

    // maxPhase - 1 because: nrOfJoins == maxPhase - 1
    for(i <- 0 to math.max(0, edmd.maxPhase-1)) {
      log.info("Running iteration: " + i)
      log.info("Output directory: " + out + Consts.fileSeparator + i)
      res = ToolRunner.run(conf, new Phase3(), Array(edmd.maxPhase.toString, i.toString, in, out))
    }

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")
    res
  }
}