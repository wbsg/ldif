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

package ldif.hadoop.entitybuilder.phases

import ldif.hadoop.entitybuilder.mappers._
import org.apache.hadoop.mapred._
import lib.{NullOutputFormat, MultipleOutputs}
import org.apache.hadoop.util._
import org.apache.hadoop.conf._
import ldif.hadoop.types._
import ldif.hadoop.entitybuilder.io._
import ldif.hadoop.utils.HadoopHelper
import ldif.entity.{EntityDescription, EntityDescriptionMetadata, EntityDescriptionMetaDataExtractor}
import org.slf4j.LoggerFactory
import ldif.hadoop.io.{QuadSequenceFileOutput, QuadSequenceFileInput}
import org.apache.hadoop.fs.{FileSystem, Path}
import ldif.util.Consts
import ldif.hadoop.runtime.ConfigParameters
import org.apache.hadoop.io.{Text, NullWritable, IntWritable}

/**
 *  Hadoop EntityBuilder - Phase 2
 *  Filtering quads and creating initial value paths
 **/

class Phase2 extends Configured with Tool {
  def run(args: Array[String]): Int = {
    val conf = getConf
    val job = new JobConf(conf, classOf[Phase2])
    val getsTextInput = args(6).toBoolean
    val useLzoInputFormat = args(7).toBoolean

    job.setJobName("HEB-Phase2")
    job.setNumReduceTasks(0)

    if(getsTextInput) {
      if(useLzoInputFormat)
        job.setInputFormat(Class.forName("com.hadoop.mapred.DeprecatedLzoTextInputFormat").asSubclass(classOf[InputFormat[Long, Text]]))
      job.setMapperClass(classOf[ExtractAndProcessQuadsMapper])

      // Check if sameAs links should be collected and add output collector
      if (args(2).toBoolean) {
        MultipleOutputs.addNamedOutput(job, "sameas", classOf[QuadSequenceFileOutput], classOf[NullWritable], classOf[QuadWritable])
        job.setBoolean("sameas", true)
      }
      // Check if irrelevant quads should be collected and add output collector
      if (args(3).toBoolean ) {
        job.setBoolean("allquads", true)

        MultipleOutputs.addNamedOutput(job, "allquads", classOf[QuadSequenceFileOutput], classOf[NullWritable], classOf[QuadWritable])
      }
      // Check if provencance quads should be ignored and add output collector
      job.setStrings("provenanceGraph", args(5))
      if (args(4).toBoolean)
        job.setBoolean("ignoreProvenance", true)
      else
        MultipleOutputs.addNamedOutput(job, "provenance", classOf[QuadSequenceFileOutput], classOf[NullWritable], classOf[QuadWritable])
    }
    else {
      job.setMapperClass(classOf[ProcessQuadsMapper])
      job.setInputFormat(classOf[QuadSequenceFileInput])
    }
    job.setMapOutputKeyClass(classOf[IntWritable])
    job.setMapOutputValueClass(classOf[ValuePathWritable])
    job.setOutputKeyClass(classOf[IntWritable])
    job.setOutputValueClass(classOf[ValuePathWritable])
    job.setOutputFormat(classOf[NullOutputFormat[IntWritable, ValuePathWritable]])

    MultipleOutputs.addNamedOutput(job, "seq", classOf[JoinValuePathMultipleSequenceFileOutput], classOf[IntWritable], classOf[ValuePathWritable])
    MultipleOutputs.addNamedOutput(job, "text", classOf[JoinValuePathMultipleTextFileOutput], classOf[IntWritable], classOf[ValuePathWritable])

    val in = new Path(args(0))
    val out = new Path(args(1))
    FileInputFormat.addInputPath(job, in)
    FileOutputFormat.setOutputPath(job, out)



    JobClient.runJob(job)

    return 0
  }
}

object Phase2 {
  private val log = LoggerFactory.getLogger(getClass.getName)


  def runPhase(in : String, out : String, entityDescriptions : Seq[EntityDescription],  config : ConfigParameters) : Int = {
    val edmd = EntityDescriptionMetaDataExtractor.extract(entityDescriptions)
    runPhase(in,out,edmd, config)
  }

  def runPhase(in : String, out : String, edmd : EntityDescriptionMetadata, config : ConfigParameters) : Int = {
    val conf = new Configuration()
    val hdfs = FileSystem.get(conf)

    // Don't collect sameAs/all quads if the destination paths are not defined (eg. while building entities for Silk)
    val useExternalSameAsLinks = config.sameAsPath != null
    val outputAllQuads = config.allQuadsPath != null
    val ignoreProvenance = config.provenanceQuadsPath == null
    val provenanceGraph = config.configProperties.getProperty("provenanceGraph", Consts.DEFAULT_PROVENANCE_GRAPH)
    val useLzoInputFormat = config.configProperties.getProperty("useLzoInputFormat", "false").toLowerCase=="true"

    log.info("Starting phase 2 of the EntityBuilder: Filtering quads and creating initial value paths")

    val start = System.currentTimeMillis
    HadoopHelper.distributeSerializableObject(edmd, conf, "edmd")

    // remove existing output
    val hdPath = new Path(out)
    if (hdfs.exists(hdPath))
      hdfs.delete(hdPath, true)

    log.info("Output directory: " + out)
    val res = ToolRunner.run(conf, new Phase2(), Array[String](in, out, useExternalSameAsLinks.toString, outputAllQuads.toString, ignoreProvenance.toString, provenanceGraph, config.getsTextInput.toString, useLzoInputFormat.toString))

    log.info("That's it. Took " + (System.currentTimeMillis-start)/1000.0 + "s")

    // move sameAs links quads to an ad-hoc directory
    if(useExternalSameAsLinks) {
      move(hdPath, config.sameAsPath, "sameas")
    }
    // move irrelevant quads to an ad-hoc directory
    if(outputAllQuads) {
      move(hdPath, config.allQuadsPath, "allquads")
    }
    // move provenance quads to an ad-hoc directory
    if(!ignoreProvenance) {
      move(hdPath, config.provenanceQuadsPath, "provenance")
    }

    res
  }

  private def move (from : Path, to : String, prefix : String) {
    move(from, new Path(to), prefix)
  }

  private def move (from : Path, to : Path, prefix : String) {
    val hdfs = FileSystem.get(new Configuration())
    val files = hdfs.listStatus(from).filterNot(_.isDir)
    if (files.length > 0 && !hdfs.exists(to))
      hdfs.mkdirs(to)
    for (status <- files) {
      if(status.getPath.getName.startsWith(prefix))
        hdfs.rename(status.getPath, new Path(to+Consts.fileSeparator+status.getPath.getName))
    }
  }

}