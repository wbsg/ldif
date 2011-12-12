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

package ldif.hadoop.entitybuilder.phases

import ldif.hadoop.entitybuilder.mappers._
import ldif.hadoop.entitybuilder.reducers._
import org.apache.hadoop.mapred._
import lib.MultipleOutputs
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import org.apache.hadoop.io.IntWritable
import ldif.hadoop.types._
import ldif.hadoop.entitybuilder.io._
import ldif.hadoop.utils.HadoopHelper
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.util.Consts
import org.slf4j.LoggerFactory
import ldif.entity.{EntityWritable, EntityDescriptionMetadata, EntityDescription, EntityDescriptionMetaDataExtractor}
import ldif.hadoop.io.{EntityMultipleSequenceFileOutput, EntityMultipleTextFileOutput}

/**
 *  Hadoop EntityBuilder - Phase 4
 *  Assembling entities
 **/

class Phase4 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val fileSystem = FileSystem.get(conf)
    val jobConf = new JobConf(conf, classOf[Phase4])
    val maxPhase = args(0).toInt

    jobConf.setJobName("HEB-Phase4")

    jobConf.setMapperClass(classOf[EntityConstructionMapper])
    jobConf.setReducerClass(classOf[EntityConstructionReducer])

    jobConf.setMapOutputKeyClass(classOf[EntityDescriptionNodeWritable])
    jobConf.setMapOutputValueClass(classOf[ValuePathWritable])

    jobConf.setOutputKeyClass(classOf[IntWritable])
    jobConf.setOutputValueClass(classOf[EntityWritable])

    jobConf.setInputFormat(classOf[ValuePathSequenceFileInput])
    jobConf.setOutputFormat(classOf[EntityMultipleSequenceFileOutput])
    //Debugging
    MultipleOutputs.addNamedOutput(jobConf, "debugReduce", classOf[EntityMultipleTextFileOutput], classOf[IntWritable], classOf[EntityWritable])
    MultipleOutputs.addNamedOutput(jobConf, "debugMap", classOf[ValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    for(i <- 0 to math.max(0, maxPhase-1)) {
      var in = new Path(args(1) + Consts.fileSeparator + i + Consts.fileSeparator, ValuePathMultipleSequenceFileOutput.generateDirectoryNameForFinishedValuePaths(i))
      if(fileSystem.exists(in))
        FileInputFormat.addInputPath(jobConf, in)
    }

    val out = new Path(args(2))
    FileOutputFormat.setOutputPath(jobConf, out)

    JobClient.runJob(jobConf)

    return 0
  }
}

object Phase4 {
  private val log = LoggerFactory.getLogger(getClass.getName)

  def runPhase(in : String, out : String, entityDescriptions : Seq[EntityDescription]) : Int = {
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)
    runPhase(in,out,edmd)
  }

  def runPhase(in : String, out : String, edmd : EntityDescriptionMetadata) : Int = {
    log.info("Starting phase 4 of the EntityBuilder: assembling entities")

    log.info("Output directory: " + out)

    val start = System.currentTimeMillis
    val conf = new Configuration
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    // remove existing output
    val hdfs = FileSystem.get(conf)
    val hdPath = new Path(out)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    val maxPhase = edmd.maxPhase

    val res = ToolRunner.run(conf, new Phase4(), Array(maxPhase.toString, in, out))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")

    // delete output of the previous phase
    hdfs.delete(new Path(in), true)
    res
  }
}



